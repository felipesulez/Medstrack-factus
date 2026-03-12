package com.felipesulez.reto_facturacion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    // ✅ Configurables desde application.yaml sin tocar código
    @Value("${factus.http.connect-timeout-ms:5000}")
    private int connectTimeout;

    @Value("${factus.http.read-timeout-ms:15000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate(TokenInterceptor tokenInterceptor) {

        // ✅ Factory con timeouts explícitos
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout); // tiempo máximo para establecer conexión
        factory.setReadTimeout(readTimeout);        // tiempo máximo esperando respuesta de Factus

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setInterceptors(Collections.singletonList(tokenInterceptor));
        return restTemplate;
    }
}