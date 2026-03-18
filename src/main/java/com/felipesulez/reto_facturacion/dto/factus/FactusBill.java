package com.felipesulez.reto_facturacion.dto.factus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactusBill {

    private Integer id;
    private String number;

    @JsonProperty("reference_code")
    private String referenceCode;

    private String cufe;
    private String qr;

    @JsonProperty("qr_image")
    private String qrImage;

    @JsonProperty("public_url")
    private String publicUrl;

    private String validated;

    @JsonProperty("gross_value")
    private BigDecimal grossValue;

    @JsonProperty("tax_amount")
    private BigDecimal taxAmount;

    @JsonProperty("discount_amount")
    private BigDecimal discountAmount;

    private BigDecimal total;

    // Map<codigoError, descripcion> — ej: {"FAJ43b": "Nombre no coincide con RUT"}
    // Es null o vacío cuando la factura está OK sin advertencias
    private Map<String, String> errors;

    @JsonProperty("payment_form")
    private FactusCodeName paymentForm;

    @JsonProperty("payment_method")
    private FactusCodeName paymentMethod;
}