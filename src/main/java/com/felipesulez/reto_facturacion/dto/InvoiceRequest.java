package com.felipesulez.reto_facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
public class InvoiceRequest {

    @JsonProperty("numbering_range_id")
    private Integer numberingRangeId; // Opcional, el Service usa el default

    @NotBlank(message = "El código de referencia de la factura es obligatorio")
    @JsonProperty("reference_code")
    private String referenceCode;

    private String observation;

    @JsonProperty("payment_form")
    private PaymentDetailsDTO paymentForm;

    @JsonProperty("payment_method")
    private PaymentDetailsDTO paymentMethod;

    @JsonProperty("operation_type")
    private Integer operationType;

    @Valid
    @NotNull(message = "Los datos del cliente son obligatorios")
    private CustomerDTO customer;

    @Valid
    @NotEmpty(message = "La factura debe tener al menos un producto")
    private List<ItemDTO> items = new ArrayList<>();
}