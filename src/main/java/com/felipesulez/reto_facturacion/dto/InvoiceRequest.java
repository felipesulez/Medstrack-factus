package com.felipesulez.reto_facturacion.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // Fuerza a Jackson a ver todo
public class InvoiceRequest {
    @JsonProperty("numbering_range_id")
    private Integer numberingRangeId = 8;

    @JsonProperty("reference_code")
    private String referenceCode;

    @JsonProperty("observation")
    private String observation = "";

    @JsonProperty("payment_form")
    private String paymentForm = "1";

    @JsonProperty("payment_method_code")
    private String paymentMethodCode = "10";

    @JsonProperty("operation_type")
    private Integer operationType = 10;

    @JsonProperty("send_email")
    private Boolean sendEmail = false;

    @JsonProperty("customer")
    private CustomerDTO customer;

    @JsonProperty("items")
    private List<ItemDTO> items = new ArrayList<>();
}