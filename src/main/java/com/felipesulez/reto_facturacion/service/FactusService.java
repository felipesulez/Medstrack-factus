package com.felipesulez.reto_facturacion.service;

import com.felipesulez.reto_facturacion.config.FactusProperties;
import com.felipesulez.reto_facturacion.dto.*;
import com.felipesulez.reto_facturacion.dto.factus.FactusApiResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Service
public class FactusService {

    private final RestTemplate restTemplate;
    private final FactusProperties props;
    private String accessToken;
    private String refreshToken;

    public FactusService(RestTemplate restTemplate, FactusProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    @PostConstruct
    public void init() {
        try {
            login();
        } catch (Exception e) {
            log.warn("⚠️ No se pudo obtener token al arrancar: {}", e.getMessage());
        }
    }

    // --- LÓGICA DE REINTENTO ---

    private <T> T ejecutarConRetry(Supplier<T> peticion) {
        try {
            return peticion.get();
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("⚠️ Token expirado. Intentando refrescar y reintentar...");
            refrescarToken();
            return peticion.get();
        }
    }

    // --- MÉTODOS DE NEGOCIO ---

    public InvoiceResponse enviarFactura(InvoiceRequest factura) {
        enriquecerConDefaults(factura);

        if (factura.getReferenceCode() == null || factura.getReferenceCode().contains("SMOKE")) {
            factura.setReferenceCode("MS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        String url = props.getApi().getUrl() + props.getApi().getEndpoints().getValidate();
        log.info("🚀 Enviando factura Medstrack. Ref: {}", factura.getReferenceCode());

        try {
            FactusApiResponse response = ejecutarConRetry(() ->
                    restTemplate.postForObject(
                            url,
                            new HttpEntity<>(factura, crearHeaders()),
                            FactusApiResponse.class
                    )
            );
            log.info("✅ Factura validada. Número: {}", response != null && response.getData() != null
                    && response.getData().getBill() != null ? response.getData().getBill().getNumber() : "?");
            return InvoiceResponse.from(response);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 409) {
                log.warn("⚠️ 409 Conflict — factura pendiente en sandbox. Limpiando automáticamente...");
                limpiarFacturaPendiente();
                log.info("🔁 Reintentando envío. Ref: {}", factura.getReferenceCode());
                FactusApiResponse response = ejecutarConRetry(() ->
                        restTemplate.postForObject(
                                url,
                                new HttpEntity<>(factura, crearHeaders()),
                                FactusApiResponse.class
                        )
                );
                return InvoiceResponse.from(response);
            }
            throw e;
        }
    }

    /**
     * Busca la factura más reciente en estado pendiente (status=0) y la anula.
     * Factus bloquea el rango de numeración mientras exista una factura pendiente de DIAN.
     * Se llama automáticamente cuando enviarFactura() recibe un 409.
     */
    @SuppressWarnings("unchecked")
    private void limpiarFacturaPendiente() {
        try {
            String listUrl = props.getApi().getUrl() + "/v1/bills?status=0&per_page=5";
            ResponseEntity<Map> listResponse = restTemplate.exchange(
                    listUrl, HttpMethod.GET, new HttpEntity<>(crearHeaders()), Map.class
            );

            Map<String, Object> body = listResponse.getBody();
            if (body == null) {
                log.warn("⚠️ Respuesta vacía al listar facturas pendientes");
                return;
            }

            Object dataRaw = body.get("data");
            List<Map<String, Object>> bills = null;

            if (dataRaw instanceof Map) {
                bills = (List<Map<String, Object>>) ((Map<?, ?>) dataRaw).get("data");
            } else if (dataRaw instanceof List) {
                bills = (List<Map<String, Object>>) dataRaw;
            }

            if (bills == null || bills.isEmpty()) {
                log.info("✅ No se encontraron facturas pendientes en el sandbox");
                return;
            }

            Map<String, Object> primera = bills.get(0);
            Object referenceCode = primera.get("reference_code");
            Object billNumber    = primera.get("number");
            log.info("🗑️ Eliminando factura pendiente — ref: {}, number: {}", referenceCode, billNumber);

            String deleteUrl = props.getApi().getUrl() + "/v1/bills/destroy/reference/" + referenceCode;
            try {
                restTemplate.exchange(
                        deleteUrl, HttpMethod.DELETE, new HttpEntity<>(crearHeaders()), Map.class
                );
                log.info("✅ Factura pendiente eliminada. Rango de numeración desbloqueado.");
            } catch (HttpClientErrorException deleteEx) {
                log.error("❌ DELETE falló ({}) — body: {}", deleteEx.getStatusCode(), deleteEx.getResponseBodyAsString());
                throw deleteEx;
            }

        } catch (Exception e) {
            log.error("❌ No se pudo limpiar la factura pendiente automáticamente: {}", e.getMessage());
            throw new RuntimeException(
                    "Sandbox bloqueado. Ve a sandbox.factus.com.co → Facturas → " +
                            "abre la factura en estado Pendiente y usa 'Anular'.", e
            );
        }
    }

    /**
     * Descarga el PDF de una factura desde Factus.
     * Factus responde con JSON: { "data": { "pdf_base_64_encoded": "JVBERi0x..." } }
     */
    @SuppressWarnings("unchecked")
    public byte[] descargarFacturaPdf(String number) {
        String url = props.getApi().getUrl() + "/v1/bills/download-pdf/" + number;
        log.info("📂 Solicitando PDF a Factus para: {}", number);

        return ejecutarConRetry(() -> {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(crearHeaders()), Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                log.warn("⚠️ Factus devolvió respuesta vacía para {}", number);
                return null;
            }

            Map<String, Object> data = (Map<String, Object>) body.get("data");
            if (data == null || !data.containsKey("pdf_base_64_encoded")) {
                log.error("❌ Respuesta sin pdf_base_64_encoded para {}: {}", number, body);
                return null;
            }

            String base64   = (String) data.get("pdf_base_64_encoded");
            byte[] pdfBytes = java.util.Base64.getDecoder().decode(base64.trim());
            log.info("✅ PDF decodificado: {} bytes para {}", pdfBytes.length, number);
            return pdfBytes;
        });
    }

    // --- GESTIÓN DE TOKENS ---

    public void login() {
        String url = props.getApi().getUrl() + props.getApi().getEndpoints().getAuth();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type",    "password");
        body.add("client_id",     props.getApi().getAuth().getClientId());
        body.add("client_secret", props.getApi().getAuth().getClientSecret());
        body.add("username",      props.getApi().getCredentials().getUsername());
        body.add("password",      props.getApi().getCredentials().getPassword());

        try {
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            if (response != null) {
                this.accessToken  = (String) response.get("access_token");
                this.refreshToken = (String) response.get("refresh_token");
                log.info("✅ Sesión iniciada exitosamente.");
            }
        } catch (Exception e) {
            log.error("❌ Falló el login: {}", e.getMessage());
            throw e;
        }
    }

    public void refrescarToken() {
        if (this.refreshToken == null) {
            login();
            return;
        }

        String url = props.getApi().getUrl() + props.getApi().getEndpoints().getAuth();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type",    "refresh_token");
        body.add("client_id",     props.getApi().getAuth().getClientId());
        body.add("client_secret", props.getApi().getAuth().getClientSecret());
        body.add("refresh_token", this.refreshToken);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            if (response != null && response.containsKey("access_token")) {
                this.accessToken  = (String) response.get("access_token");
                this.refreshToken = (String) response.get("refresh_token");
                log.info("🔄 Token renovado automáticamente.");
            }
        } catch (Exception e) {
            log.error("❌ No se pudo refrescar el token, reintentando login completo...");
            login();
        }
    }

    private HttpHeaders crearHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        if (this.accessToken == null) login();
        headers.setBearerAuth(this.accessToken);
        return headers;
    }

    public String getAccessToken() { return this.accessToken; }

    private void enriquecerConDefaults(InvoiceRequest f) {
        FactusProperties.Defaults d = props.getDefaults();

        if (f.getNumberingRangeId() == null) f.setNumberingRangeId(d.getNumberingRangeId());
        if (f.getPaymentForm()      == null) f.setPaymentForm(new PaymentDetailsDTO(d.getPaymentForm()));
        if (f.getPaymentMethod()    == null) f.setPaymentMethod(new PaymentDetailsDTO(d.getPaymentMethodCode()));
        if (f.getOperationType()    == null) f.setOperationType(10);

        if (f.getCustomer() != null) {
            CustomerDTO c = f.getCustomer();
            if (c.getMunicipalityId()          == null) c.setMunicipalityId(d.getMunicipalityId());
            if (c.getLegalOrganizationId()     == null) c.setLegalOrganizationId("1");
            if (c.getTributeId()               == null) c.setTributeId("21");
            if (c.getIdentificationDocumentId()== null) {
                c.setIdentificationDocumentId(c.getIdentification().length() > 9 ? "6" : "3");
            }
            if ("6".equals(c.getIdentificationDocumentId()) && (c.getDv() == null || c.getDv().isEmpty())) {
                c.setDv(com.felipesulez.reto_facturacion.util.NitUtils.calcularDV(c.getIdentification()));
            }
        }

        if (f.getItems() != null) {
            for (ItemDTO item : f.getItems()) {
                if (item.getDiscountRate()  == null) item.setDiscountRate(BigDecimal.ZERO);
                if (item.getTaxRate()       == null) item.setTaxRate(new BigDecimal("19.00"));
                if (item.getIsExcluded()    == null) item.setIsExcluded(0);
                if (item.getUnitMeasureId() == null) item.setUnitMeasureId(70);
                if (item.getStandardCodeId()== null) item.setStandardCodeId(1);
                if (item.getTributeId()     == null) item.setTributeId(1);
                if (item.getCodeReference() == null)
                    item.setCodeReference("REF-" + UUID.randomUUID().toString().substring(0, 5));
            }
        }
    }
}