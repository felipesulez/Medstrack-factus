package com.felipesulez.reto_facturacion.controller;

import com.felipesulez.reto_facturacion.dto.InvoiceRequest;
import com.felipesulez.reto_facturacion.dto.InvoiceResponse;
import com.felipesulez.reto_facturacion.service.FactusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Facturación", description = "Endpoints para Medstrack — envío y descarga de facturas electrónicas")
public class InvoiceController {

    private final FactusService factusService;

    @PostMapping("/send")
    @Operation(
            summary = "Emitir factura electrónica",
            description = """
                    Valida y registra la factura en Factus/DIAN.
                    
                    El sistema calcula automáticamente el dígito de verificación (DV) del NIT,
                    aplica la forma de pago por defecto (contado/efectivo) si no se indica,
                    y rellena los campos técnicos requeridos por la DIAN.
                    
                    Si el sandbox devuelve un 409 (rango de numeración bloqueado por una factura
                    pendiente), el sistema la elimina automáticamente y reintenta sin intervención manual.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Factura validada correctamente por la DIAN",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InvoiceResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos — revisar campo 'errors' en la respuesta",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Rango bloqueado — el sistema lo resuelve automáticamente y reintenta",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Factus rechazó la factura — revisar campo 'data' con el detalle del error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    public ResponseEntity<InvoiceResponse> enviarFactura(@Valid @RequestBody InvoiceRequest request) {
        return ResponseEntity.ok(factusService.enviarFactura(request));
    }

    @GetMapping(value = "/download-pdf/{number}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Descargar PDF de una factura",
            description = """
                    Recupera el PDF binario desde Factus y lo devuelve listo para descarga.
                    
                    Usa el número de factura asignado por la DIAN (ej: SETP990025918).
                    El archivo se sirve con Content-Disposition: attachment, por lo que el navegador
                    lo descarga directamente sin abrirlo en una pestaña nueva.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "PDF generado correctamente",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_PDF_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    ),
                    headers = {
                            @Header(name = "Content-Disposition",
                                    description = "attachment; filename=\"factura-{number}.pdf\"",
                                    schema = @Schema(type = "string")),
                            @Header(name = "Content-Type",
                                    description = "application/pdf",
                                    schema = @Schema(type = "string"))
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Factura no encontrada en Factus",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token expirado — el sistema reintenta automáticamente",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    public ResponseEntity<byte[]> descargarFactura(
            @Parameter(
                    description = "Número de factura asignado por Factus/DIAN (ej: SETP990025918)",
                    example = "SETP990025918",
                    required = true
            )
            @PathVariable String number) {

        byte[] pdfBytes = factusService.descargarFacturaPdf(number);

        if (pdfBytes == null || pdfBytes.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentLength(pdfBytes.length);
        headers.setContentDispositionFormData("attachment", "factura-" + number + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @Profile("dev")
    @GetMapping("/debug/token")
    @Operation(summary = "Ver token actual (solo perfil dev)")
    public ResponseEntity<Map<String, String>> obtenerTokenActual() {
        String token = factusService.getAccessToken();
        return (token != null)
                ? ResponseEntity.ok(Map.of("token", token))
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}