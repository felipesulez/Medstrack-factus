package com.felipesulez.reto_facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ItemDTO {
    @JsonProperty("code_reference")
    private String codeReference;

    @JsonProperty("name")
    private String name;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("tax_rate")
    private String taxRate = "19.00";

    @JsonProperty("discount_rate")
    private Double discountRate = 0.0;

    @JsonProperty("is_excluded")
    private Integer isExcluded = 0;

    @JsonProperty("unit_measure_id")
    private Integer unitMeasureId = 70;

    @JsonProperty("standard_code_id")
    private Integer standardCodeId = 1;

    @JsonProperty("tribute_id")
    private Integer tributeId = 1;

    // MÉTODO MANUAL PARA ASEGURAR LA CONEXIÓN CON JACKSON
    @JsonProperty("name")
    public String getName() {
        return name;
    }
}