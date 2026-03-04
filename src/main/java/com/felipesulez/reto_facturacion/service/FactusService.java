package com.felipesulez.reto_facturacion.service;

import com.felipesulez.reto_facturacion.dto.InvoiceRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Map;

@Slf4j
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
        log.info("📡 Iniciando proceso de autenticación en Factus (Login completo)...");

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
            log.error("❌ Error crítico durante el login: {}", e.getMessage());
        }
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void refrescarToken() {
        log.info("🔄 Ejecutando Refresh Token Flow...");

        // Si no tenemos un refresh token guardado, no podemos refrescar, hacemos login
        if (this.refreshToken == null) {
            log.warn("⚠️ No hay Refresh Token disponible. Reintentando Login completo...");
            login();
            return;
        }

        String url = apiUrl + "/oauth/token";
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
                log.info("✅ ¡ÉXITO! Token renovado usando el Refresh Token original.");
            }
        } catch (Exception e) {
            log.error("❌ El Refresh Token falló. Forzando Login completo...");
            login();
        }
    }

    public Map<String, Object> enviarFactura(InvoiceRequest factura) {
        String url = apiUrl + "/v1/bills/validate";
        log.info("🚀 Intentando enviar factura: {}", factura.getReferenceCode());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // TIP PRO: Manejo seguro de nulidad.
        // Si el token es null, mandamos un String vacío. Esto causará un 401 controlado
        // por el Interceptor, el cual llamará a refrescarToken() o login() automáticamente.
        String currentToken = (this.accessToken != null) ? this.accessToken : "";
        headers.setBearerAuth(currentToken);

        HttpEntity<InvoiceRequest> request = new HttpEntity<>(factura, headers);

        // El interceptor se encarga de reintentar si esto devuelve 401
        return restTemplate.postForObject(url, request, Map.class);
    }
}