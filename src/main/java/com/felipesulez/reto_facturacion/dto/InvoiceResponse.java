package com.felipesulez.reto_facturacion.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.felipesulez.reto_facturacion.dto.factus.FactusApiResponse;
import com.felipesulez.reto_facturacion.dto.factus.FactusBill;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resultado de la factura validada en Factus/DIAN")
public class InvoiceResponse {

    @Schema(description = "Número de factura asignado por la DIAN", example = "SETP990025769")
    private String numero;

    @Schema(description = "Código de referencia interno", example = "INV-1001")
    private String referenceCode;

    @Schema(
            description = "CUFE — Código Único de Factura Electrónica",
            example = "044059fbe14f7a2577956131724141d816e082b3c68c30d884ba599b3ac3ac49..."
    )
    private String cufe;

    @Schema(description = "URL pública del portal DIAN para consulta", example = "http://app-sandbox.factus.com.co/documents/bills/...")
    private String publicUrl;

    @Schema(description = "Imagen del código QR en Base64 (data:image/png;base64,...)")
    private String qrImageBase64;

    @Schema(description = "Estado de la validación", example = "VALIDADA", allowableValues = {"VALIDADA", "VALIDADA CON ADVERTENCIAS"})
    private String estado;

    @Schema(description = "Fecha y hora de validación por la DIAN", example = "09-01-2025 01:56:16 PM")
    private String fechaValidacion;

    @Schema(description = "Valor total de la factura", example = "50000.00")
    private BigDecimal total;

    @Schema(description = "Total de impuestos aplicados", example = "7983.19")
    private BigDecimal totalImpuestos;

    @Schema(description = "Advertencias no bloqueantes de la DIAN — null si no hay ninguna")
    private Map<String, String> advertencias;

    // -------------------------------------------------------------------------
    // Factory method — convierte la respuesta cruda de Factus en este DTO limpio
    // -------------------------------------------------------------------------
    public static InvoiceResponse from(FactusApiResponse factusResponse) {
        if (factusResponse == null || factusResponse.getData() == null) {
            return InvoiceResponse.builder()
                    .estado("ERROR")
                    .build();
        }

        FactusBill bill = factusResponse.getData().getBill();

        if (bill == null) {
            return InvoiceResponse.builder()
                    .estado("ERROR")
                    .build();
        }

        boolean tieneAdvertencias = bill.getErrors() != null && !bill.getErrors().isEmpty();

        return InvoiceResponse.builder()
                .numero(bill.getNumber())
                .referenceCode(bill.getReferenceCode())
                .cufe(bill.getCufe())
                .publicUrl(bill.getPublicUrl())
                .qrImageBase64(bill.getQrImage())
                .estado(tieneAdvertencias ? "VALIDADA CON ADVERTENCIAS" : "VALIDADA")
                .fechaValidacion(bill.getValidated())
                .total(bill.getTotal())
                .totalImpuestos(bill.getTaxAmount())
                .advertencias(tieneAdvertencias ? bill.getErrors() : null)
                .build();
    }
}