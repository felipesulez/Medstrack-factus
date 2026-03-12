package com.felipesulez.reto_facturacion.config;

import com.felipesulez.reto_facturacion.service.FactusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod; // Importante
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap; // Importante
import java.util.Map;     // Importante

@Slf4j
@Component
public class TokenInterceptor implements ClientHttpRequestInterceptor {

    private final FactusService factusService;

    public TokenInterceptor(@Lazy FactusService factusService) {
        this.factusService = factusService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);

        // En Spring Boot 3 se recomienda usar isSameCodeAs para comparar HttpStatus
        if (response.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
            log.warn("⚠️ Token expirado detectado. Iniciando autoreparación...");

            synchronized (this) {
                factusService.refrescarToken();

                HttpRequest nuevaRequest = new HttpRequestWrapper(request, factusService.getAccessToken());

                log.info("🔄 Reintentando operación con nuevo token...");
                return execution.execute(nuevaRequest, body);
            }
        }

        return response;
    }

    private static class HttpRequestWrapper implements HttpRequest {

        private final HttpRequest original;
        private final HttpHeaders headers;

        public HttpRequestWrapper(HttpRequest original, String nuevoToken) {
            this.original = original;
            this.headers = new HttpHeaders();
            this.headers.addAll(original.getHeaders());
            this.headers.setBearerAuth(nuevoToken);
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.headers;
        }

        /* ✅ CORRECCIÓN 1: El tipo de retorno debe ser HttpMethod, no String */
        @Override
        public HttpMethod getMethod() {
            return original.getMethod();
        }

        /* ✅ CORRECCIÓN 2: Método obligatorio en Spring Boot 3 */
        @Override
        public Map<String, Object> getAttributes() {
            return new HashMap<>(); // O return original.getAttributes() si existiera
        }

        @Override
        public URI getURI() {
            return original.getURI();
        }
    }
}