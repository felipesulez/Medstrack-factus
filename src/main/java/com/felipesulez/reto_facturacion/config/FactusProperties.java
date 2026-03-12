package com.felipesulez.reto_facturacion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "factus")
public class FactusProperties {

    private Api api = new Api();
    private Defaults defaults = new Defaults();
    private Http http = new Http();

    @Data
    public static class Api {

        @NotBlank(message = "La URL de Factus es obligatoria")
        private String url;

        private Endpoints endpoints = new Endpoints();
        private Auth auth = new Auth();
        private Credentials credentials = new Credentials();

        @Data
        public static class Endpoints {
            private String auth = "/oauth/token";
            private String validate = "/v1/bills/validate";
        }

        @Data
        public static class Auth {
            @NotBlank(message = "El client-id de Factus es obligatorio")
            private String clientId;

            @NotBlank(message = "El client-secret de Factus es obligatorio")
            private String clientSecret;
        }

        @Data
        public static class Credentials {
            @NotBlank(message = "El username de Factus es obligatorio")
            private String username;

            @NotBlank(message = "El password de Factus es obligatorio")
            private String password;
        }
    }

    @Data
    public static class Defaults {
        @NotNull
        private Integer numberingRangeId = 8;
        private String municipalityId = "980";
        private String paymentForm = "1";
        private String paymentMethodCode = "10";
        private Integer operationType = 10;
    }

    @Data
    public static class Http {
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 15000;
    }
}