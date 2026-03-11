package com.felipesulez.reto_facturacion.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private int status;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private Map<String, String> errors;

    // MEJORA: Añadimos este campo para capturar la respuesta técnica de Factus
    private Object data;
}