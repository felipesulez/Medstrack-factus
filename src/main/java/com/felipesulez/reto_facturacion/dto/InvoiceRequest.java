package com.felipesulez.reto_facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
@Schema(description = "Datos necesarios para emitir una factura electrónica en Factus")
public class InvoiceRequest {

    @JsonProperty("numbering_range_id")
    @Schema(
            description = "ID del rango de numeración en Factus. Opcional — el sistema usa el rango por defecto (8) si se omite.",
            example = "8",
            nullable = true
    )
    private Integer numberingRangeId;

    @NotBlank(message = "El código de referencia de la factura es obligatorio")
    @JsonProperty("reference_code")
    @Schema(
            description = "Código de referencia interno único para esta factura. Lo genera el sistema automáticamente si se omite.",
            example = "PROY-MEDS-2026"
    )
    private String referenceCode;

    @Schema(
            description = "Observación o nota libre que aparece en la factura.",
            example = "Servicios de consultoría técnica plataforma Medstrack",
            nullable = true
    )
    private String observation;

    @JsonProperty("payment_form")
    @Schema(
            description = "Forma de pago. Opcional — por defecto '1' (Contado). Valores: 1=Contado, 2=Crédito.",
            example = "1",
            nullable = true
    )
    private PaymentDetailsDTO paymentForm;

    @JsonProperty("payment_method")
    @Schema(
            description = "Método de pago. Opcional — por defecto '10' (Efectivo). Valores: 10=Efectivo, 42=Débito, 48=Tarjeta crédito.",
            example = "10",
            nullable = true
    )
    private PaymentDetailsDTO paymentMethod;

    @JsonProperty("operation_type")
    @Schema(
            description = "Tipo de operación DIAN. Opcional — por defecto 10 (Estándar).",
            example = "10",
            nullable = true
    )
    private Integer operationType;

    @JsonProperty("payment_due_date")
    @Schema(
            description = "Fecha de vencimiento del pago. Obligatorio cuando payment_form es '2' (Crédito). Formato: YYYY-MM-DD.",
            example = "2026-04-12",
            nullable = true
    )
    private String paymentDueDate;

    @Valid
    @NotNull(message = "Los datos del cliente son obligatorios")
    private CustomerDTO customer;

    @Valid
    @NotEmpty(message = "La factura debe tener al menos un producto")
    private List<ItemDTO> items = new ArrayList<>();
}