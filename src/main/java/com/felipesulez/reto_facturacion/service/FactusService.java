package com.felipesulez.reto_facturacion.service;

import com.felipesulez.reto_facturacion.config.FactusProperties;
import com.felipesulez.reto_facturacion.dto.*;
import com.felipesulez.reto_facturacion.util.NitUtils;
import lombok.extern.slf4j.Slf4j;
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
    private final FactusProperties props; // ✅ Un solo objeto en lugar de 8 @Value

    private String accessToken;
    private String refreshToken;

    public FactusService(RestTemplate restTemplate, FactusProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    public Map<String, Object> enviarFactura(InvoiceRequest factura) {
        enriquecerConDefaults(factura);
        String url = props.getApi().getUrl() + props.getApi().getEndpoints().getValidate();
        log.info("🚀 Enviando factura Medstrack. Ref: {}", factura.getReferenceCode());

        try {
            return restTemplate.postForObject(url, new HttpEntity<>(factura, crearHeaders()), Map.class);
        } catch (HttpClientErrorException.UnprocessableEntity | HttpClientErrorException.Conflict e) {
            log.warn("⚠️ Conflicto detectado. Limpiando referencia: {}", factura.getReferenceCode());
            eliminarFacturaPorReferencia(factura.getReferenceCode());
            throw e;
        }
    }

    private void enriquecerConDefaults(InvoiceRequest f) {
        FactusProperties.Defaults d = props.getDefaults();

        if (f.getNumberingRangeId() == null) f.setNumberingRangeId(d.getNumberingRangeId());
        if (f.getPaymentForm() == null) f.setPaymentForm(new PaymentDetailsDTO(d.getPaymentForm()));
        if (f.getPaymentMethod() == null) f.setPaymentMethod(new PaymentDetailsDTO(d.getPaymentMethodCode()));
        if (f.getOperationType() == null) f.setOperationType(d.getOperationType());

        if (f.getCustomer() != null) {
            CustomerDTO c = f.getCustomer();
            if (c.getMunicipalityId() == null) c.setMunicipalityId(d.getMunicipalityId());
            if (c.getLegalOrganizationId() == null) c.setLegalOrganizationId("1");
            if (c.getTributeId() == null) c.setTributeId("21");

            if (c.getIdentificationDocumentId() == null) {
                c.setIdentificationDocumentId(c.getIdentification().length() > 9 ? "6" : "3");
            }

            if ("6".equals(c.getIdentificationDocumentId()) && (c.getDv() == null || c.getDv().isEmpty())) {
                c.setDv(NitUtils.calcularDV(c.getIdentification()));
            }
        }

        if (f.getItems() != null) {
            for (ItemDTO item : f.getItems()) {
                if (item.getCodeReference() == null) item.setCodeReference("REF-" + System.currentTimeMillis());
                if (item.getTaxRate() == null) item.setTaxRate(new BigDecimal("19.00"));
                if (item.getDiscountRate() == null) item.setDiscountRate(BigDecimal.ZERO);
                if (item.getUnitMeasureId() == null) item.setUnitMeasureId(70);
                if (item.getIsExcluded() == null) item.setIsExcluded(0);
                if (item.getStandardCodeId() == null) item.setStandardCodeId(1);
                if (item.getTributeId() == null) item.setTributeId(1);
            }
        }
    }

    public void login() {
        String url = props.getApi().getUrl() + props.getApi().getEndpoints().getAuth();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", props.getApi().getAuth().getClientId());
        body.add("client_secret", props.getApi().getAuth().getClientSecret());
        body.add("username", props.getApi().getCredentials().getUsername());
        body.add("password", props.getApi().getCredentials().getPassword());

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

    public void refrescarToken() {
        if (this.refreshToken == null) {
            login();
            return;
        }

        String url = props.getApi().getUrl() + props.getApi().getEndpoints().getAuth();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", props.getApi().getAuth().getClientId());
        body.add("client_secret", props.getApi().getAuth().getClientSecret());
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

    public void eliminarFacturaPorReferencia(String ref) {
        try {
            restTemplate.exchange(
                    props.getApi().getUrl() + "/v1/bills/destroy/reference/" + ref,
                    HttpMethod.DELETE,
                    new HttpEntity<>(crearHeaders()),
                    Map.class
            );
            log.info("🧹 Limpieza de referencia {} exitosa.", ref);
        } catch (Exception e) {
            log.error("❌ No se pudo eliminar la referencia {}: {}", ref, e.getMessage());
        }
    }

    public String getAccessToken() {
        return this.accessToken;
    }
}