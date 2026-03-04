package com.felipesulez.reto_facturacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean
    public RestTemplate restTemplate(TokenInterceptor tokenInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(java.util.Collections.singletonList(tokenInterceptor));
        return restTemplate;
    }
}