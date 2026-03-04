package com.felipesulez.reto_facturacion.service;

import com.felipesulez.reto_facturacion.dto.InvoiceRequest;
import lombok.extern.slf4j.Slf4j; // Importamos el logger de Lombok
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Map;

@Slf4j // Genera automáticamente la variable 'log'
@Service
public class FactusService {

    private final RestTemplate restTemplate;

    @Value("${factus.api.url}")
    private String apiUrl;

    @Value("${factus.api.auth.client-id}")
    private String clientId;

    @Value("${factus.api.auth.client-secret}")
    private String clientSecret;

    @Value("${factus.api.credentials.username}")
    private String username;

    @Value("${factus.api.credentials.password}")
    private String password;

    private String accessToken;
    private String refreshToken;

    public FactusService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void login() {
        String url = apiUrl + "/oauth/token";
        log.info("Iniciando proceso de autenticación en Factus..."); // Log profesional

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
                log.info("✅ Login exitoso. Token obtenido correctamente.");
            }
        } catch (Exception e) {
            log.error("❌ Error durante el login: {}", e.getMessage()); // Log de error con variable
        }
    }

    // Dentro de FactusService.java

    public String getAccessToken() {
        return this.accessToken;
    }

    public void refrescarToken() {
        log.info("📡 Solicitando renovación de token mediante refresh_token...");
        String url = apiUrl + "/oauth/token";

        org.springframework.util.MultiValueMap<String, String> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", this.refreshToken);

        try {
            Map<String, Object> response = restTemplate.postForObject(url, body, Map.class);
            if (response != null) {
                this.accessToken = (String) response.get("access_token");
                this.refreshToken = (String) response.get("refresh_token");
                log.info("✅ Token renovado exitosamente.");
            }
        } catch (Exception e) {
            log.error("❌ Refresh Token fallido. Intentando Login completo...");
            login();
        }
    }



    public Map<String, Object> enviarFactura(InvoiceRequest factura) {
        String url = apiUrl + "/v1/bills/validate";
        log.info("Enviando factura con referencia: {}", factura.getReferenceCode()); // Referencia limpia

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<InvoiceRequest> request = new HttpEntity<>(factura, headers);
        return restTemplate.postForObject(url, request, Map.class);
    }
}