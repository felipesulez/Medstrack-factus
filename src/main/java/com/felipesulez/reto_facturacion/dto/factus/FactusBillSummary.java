package com.felipesulez.reto_facturacion.dto.factus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Resumen de una factura en el listado")
public class FactusBillSummary {

    @Schema(description = "ID interno de Factus", example = "36076")
    private Integer id;

    private FactusCodeName document;

    @Schema(description = "Número de factura DIAN", example = "SETP990026624")
    private String number;

    @JsonProperty("reference_code")
    @Schema(description = "Código de referencia interno", example = "MEDS-TEST-001")
    private String referenceCode;

    @Schema(description = "NIT o cédula del cliente", example = "123456789")
    private String identification;

    @JsonProperty("graphic_representation_name")
    @Schema(description = "Nombre para representación gráfica")
    private String graphicRepresentationName;

    private String company;

    @JsonProperty("trade_name")
    private String tradeName;

    private String names;
    private String email;

    @Schema(description = "Total de la factura", example = "50000.00")
    private BigDecimal total;

    @Schema(description = "Estado: 1=validada, 0=pendiente", example = "1",
            allowableValues = {"0", "1"})
    private Integer status;

    // En el listado errors es List<String> — diferente al POST que es Map<String,String>
    @Schema(description = "Advertencias DIAN — lista vacía si no hay ninguna")
    // DESPUÉS
    private Object errors;
    @JsonProperty("send_email")
    private Integer sendEmail;

    @JsonProperty("has_claim")
    private Integer hasClaim;

    @JsonProperty("is_negotiable_instrument")
    private Integer isNegotiableInstrument;

    @JsonProperty("payment_form")
    private FactusCodeName paymentForm;

    @JsonProperty("created_at")
    @Schema(description = "Fecha de creación", example = "18-03-2026 05:11:07 PM")
    private String createdAt;

    @JsonProperty("credit_notes")
    private List<FactusRelatedDocument> creditNotes;

    @JsonProperty("debit_notes")
    private List<FactusRelatedDocument> debitNotes;
}