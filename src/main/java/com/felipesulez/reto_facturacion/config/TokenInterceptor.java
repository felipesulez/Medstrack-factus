package com.felipesulez.reto_facturacion.config;

import com.felipesulez.reto_facturacion.service.FactusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class TokenInterceptor implements ClientHttpRequestInterceptor {

    private final FactusService factusService;

    // @Lazy es vital para evitar problemas de "huevo o gallina" con el servicio
    public TokenInterceptor(@Lazy FactusService factusService) {
        this.factusService = factusService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);

        // Si la respuesta es 401, renovamos y reintentamos
        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            log.warn("⚠️ Token expirado detectado. Iniciando autoreparación...");

            synchronized (this) {
                factusService.refrescarToken();
                request.getHeaders().setBearerAuth(factusService.getAccessToken());

                log.info("🔄 Reintentando operación con nuevo token...");
                return execution.execute(request, body);
            }
        }

        return response;
    }
}