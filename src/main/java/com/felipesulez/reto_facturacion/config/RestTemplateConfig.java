package com.felipesulez.reto_facturacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(TokenInterceptor tokenInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        // Con esto, cada petición pasará por tu lógica de auto-reparación de token
        restTemplate.setInterceptors(Collections.singletonList(tokenInterceptor));
        return restTemplate;
    }
}