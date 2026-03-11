package com.felipesulez.reto_facturacion.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    /**
     * Captura errores de la API externa (Factus) 4xx y 5xx.
     * Consolidado para procesar el JSON de error de forma limpia.
     */
    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<ErrorResponse> handleHttpSignatureError(HttpStatusCodeException ex, HttpServletRequest request) {
        String rawBody = ex.getResponseBodyAsString();
        Object cleanData;

        try {
            // Intentamos convertir el JSON de Factus en un objeto real
            cleanData = objectMapper.readValue(rawBody, Object.class);
        } catch (Exception e) {
            // Si no es JSON o falla, limpiamos los escapes manualmente
            cleanData = cleanErrorMessage(rawBody);
        }

        ErrorResponse response = ErrorResponse.builder()
                .status(ex.getStatusCode().value())
                .message("Error del proveedor de facturación (Factus)")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .data(cleanData)
                .build();

        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    private String cleanErrorMessage(String body) {
        if (body == null) return "Sin detalle";
        return body.replace("\\\"", "\"");
    }

    /**
     * Captura validaciones de campos locales (@Valid).
     * Intercepta las reglas de BigDecimal y Min 0.01 de Medstrack.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validación fallida: Los datos no cumplen con los requisitos")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .errors(fieldErrors)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Error inesperado en Medstrack: " + ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}