package com.felipesulez.reto_facturacion.service;

import com.felipesulez.reto_facturacion.dto.*;
import com.felipesulez.reto_facturacion.util.NitUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
public class FactusService {

    private final RestTemplate restTemplate;

    @Value("${factus.api.url}")
    private String apiUrl;

    @Value("${factus.api.endpoints.auth}")
    private String authPath;

    @Value("${factus.api.endpoints.validate}")
    private String validatePath;

    @Value("${factus.api.auth.client-id}")
    private String clientId;

    @Value("${factus.api.auth.client-secret}")
    private String clientSecret;

    @Value("${factus.api.credentials.username}")
    private String username;

    @Value("${factus.api.credentials.password}")
    private String password;

    @Value("${factus.defaults.numbering-range-id:8}")
    private Integer defaultRangeId;

    @Value("${factus.defaults.municipality-id:980}")
    private String defaultMunId;

    private String accessToken;
    private String refreshToken;

    public FactusService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Envía la factura a Factus aplicando lógica de resiliencia y valores por defecto.
     */
    public Map<String, Object> enviarFactura(InvoiceRequest factura) {
        enriquecerConDefaults(factura);
        String url = apiUrl + validatePath;
        log.info("🚀 Enviando factura Medstrack. Ref: {}", factura.getReferenceCode());

        try {
            return restTemplate.postForObject(url, new HttpEntity<>(factura, crearHeaders()), Map.class);
        } catch (HttpClientErrorException.UnprocessableEntity | HttpClientErrorException.Conflict e) {
            log.warn("⚠️ Conflicto detectado o error de procesamiento. Intentando limpiar referencia: {}", factura.getReferenceCode());
            eliminarFacturaPorReferencia(factura.getReferenceCode());
            throw e;
        }
    }

    /**
     * Llena los vacíos del DTO para que el usuario no tenga que enviar campos técnicos
     * y se cumplan los requisitos de la DIAN/Factus.
     */
    private void enriquecerConDefaults(InvoiceRequest f) {
        // 1. Cabecera de Factura
        if (f.getNumberingRangeId() == null) f.setNumberingRangeId(defaultRangeId);
        if (f.getPaymentForm() == null) f.setPaymentForm(new PaymentDetailsDTO("1")); // Contado
        if (f.getPaymentMethod() == null) f.setPaymentMethod(new PaymentDetailsDTO("10")); // Efectivo
        if (f.getOperationType() == null) f.setOperationType(10); // Estándar

        // 2. Datos del Cliente
        if (f.getCustomer() != null) {
            CustomerDTO c = f.getCustomer();
            if (c.getMunicipalityId() == null) c.setMunicipalityId(defaultMunId);
            if (c.getLegalOrganizationId() == null) c.setLegalOrganizationId("1"); // Jurídica
            if (c.getTributeId() == null) c.setTributeId("21"); // No responsable de IVA

            // Determinar tipo de documento si viene vacío
            if (c.getIdentificationDocumentId() == null) {
                c.setIdentificationDocumentId(c.getIdentification().length() > 9 ? "6" : "3");
            }

            // Cálculo automático de DV para NIT
            if ("6".equals(c.getIdentificationDocumentId()) && (c.getDv() == null || c.getDv().isEmpty())) {
                c.setDv(NitUtils.calcularDV(c.getIdentification()));
            }
        }

        // 3. Detalle de Items (Soluciona el error 422)
        if (f.getItems() != null) {
            for (ItemDTO item : f.getItems()) {
                if (item.getCodeReference() == null) {
                    item.setCodeReference("REF-" + System.currentTimeMillis());
                }
                if (item.getTaxRate() == null) item.setTaxRate(new BigDecimal("19.00"));
                if (item.getDiscountRate() == null) item.setDiscountRate(BigDecimal.ZERO);
                if (item.getUnitMeasureId() == null) item.setUnitMeasureId(70); // Unidad

                // Campos obligatorios exigidos por el API de Factus (Error 422)
                if (item.getIsExcluded() == null) item.setIsExcluded(0);
                if (item.getStandardCodeId() == null) item.setStandardCodeId(1);
                if (item.getTributeId() == null) item.setTributeId(1); // IVA
            }
        }
    }

    /**
     * Autenticación inicial contra Factus.
     */
    public void login() {
        String url = apiUrl + authPath;
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", username);
        body.add("password", password);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            if (response != null) {
                this.accessToken = (String) response.get("access_token");
                this.refreshToken = (String) response.get("refresh_token");
                log.info("✅ Sesión iniciada exitosamente.");
            }
        } catch (Exception e) {
            log.error("❌ Error en el proceso de Login: {}", e.getMessage());
        }
    }

    /**
     * Refresca el token de acceso cuando expira.
     */
    public void refrescarToken() {
        if (this.refreshToken == null) {
            login();
            return;
        }

        String url = apiUrl + authPath;
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", this.refreshToken);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            if (response != null && response.containsKey("access_token")) {
                this.accessToken = (String) response.get("access_token");
                this.refreshToken = (String) response.get("refresh_token");
                log.info("🔄 Token de acceso renovado.");
            }
        } catch (Exception e) {
            log.warn("⚠️ No se pudo refrescar el token, reintentando login completo.");
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

    /**
     * Elimina una factura fallida para permitir reintentos con la misma referencia.
     */
    public void eliminarFacturaPorReferencia(String ref) {
        try {
            restTemplate.exchange(apiUrl + "/v1/bills/destroy/reference/" + ref,
                    HttpMethod.DELETE, new HttpEntity<>(crearHeaders()), Map.class);
            log.info("🧹 Limpieza de referencia {} exitosa.", ref);
        } catch (Exception e) {
            log.error("❌ No se pudo eliminar la referencia {}: {}", ref, e.getMessage());
        }
    }

    public String getAccessToken() {
        return this.accessToken;
    }
}