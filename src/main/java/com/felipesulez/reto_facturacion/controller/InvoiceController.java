package com.felipesulez.reto_facturacion.controller;

import com.felipesulez.reto_facturacion.dto.InvoiceRequest;
import com.felipesulez.reto_facturacion.exception.ErrorResponse;
import com.felipesulez.reto_facturacion.service.FactusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Facturación", description = "Endpoints para la gestión de facturas electrónicas en Medstrack")
public class InvoiceController {

    private final FactusService factusService;

    @Operation(
            summary = "Enviar factura a Factus/DIAN",
            description = "Recibe los datos de la factura, aplica valores por defecto de Medstrack y realiza el envío al sandbox de Factus."
    )
    @ApiResponse(responseCode = "200", description = "Factura procesada y validada exitosamente")
    @ApiResponse(responseCode = "400", description = "Error de validación en los datos enviados",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Error interno del servidor o falla en la comunicación con Factus",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> enviarFactura(@Valid @RequestBody InvoiceRequest request) {
        Map<String, Object> response = factusService.enviarFactura(request);
        return ResponseEntity.ok(response);
    }

    // ✅ Solo existe en desarrollo, invisible en Railway
    @Profile("dev")
    @GetMapping("/debug/token")
    public ResponseEntity<Map<String, String>> obtenerTokenActual() {
        String token = factusService.getAccessToken();
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No hay un token activo. Primero intenta enviar una factura o haz login."));
        }
        return ResponseEntity.ok(Map.of("token_actual", token));
    }
}