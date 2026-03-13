package com.felipesulez.reto_facturacion.controller;

import com.felipesulez.reto_facturacion.dto.InvoiceRequest;
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
    @Operation(summary = "Enviar factura a Factus")
    public ResponseEntity<Map<String, Object>> enviarFactura(@Valid @RequestBody InvoiceRequest request) {
        return ResponseEntity.ok(factusService.enviarFactura(request));
    }

    /**
     * Descarga el PDF binario de una factura validada en Factus.
     *
     * La anotación produces = APPLICATION_PDF_VALUE le dice a Spring que este
     * endpoint SOLO acepta clientes que pidan application/pdf, y a OpenAPI que
     * documente el response body como binario — habilitando el botón "Download"
     * nativo de Swagger UI.
     */
    @GetMapping(value = "/download-pdf/{number}", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(
            summary = "Descargar PDF de una factura",
            description = """
                    Recupera el PDF binario desde Factus y lo devuelve listo para descarga.
                    
                    Usa el número de factura asignado por la DIAN (ej: SETP990025918) o el UUID interno.
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
                            // ✅ schema type=string format=binary es la combinación que activa
                            // el botón "Download file" en Swagger UI y en Postman
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
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "Factus no disponible o respondió con error",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    public ResponseEntity<byte[]> descargarFactura(
            @Parameter(
                    description = "Número de factura asignado por Factus/DIAN (ej: SETP990025918) o UUID interno",
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
        // Content-Disposition: attachment fuerza descarga en el navegador
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