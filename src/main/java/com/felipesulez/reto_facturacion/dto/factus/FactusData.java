package com.felipesulez.reto_facturacion.dto.factus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactusData {

    private FactusBill bill;

    @JsonProperty("numbering_range")
    private FactusNumberingRange numberingRange;

    private List<FactusItem> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactusNumberingRange {
        private String prefix;
        private Long from;
        private Long to;

        @JsonProperty("resolution_number")
        private String resolutionNumber;

        @JsonProperty("start_date")
        private String startDate;

        @JsonProperty("end_date")
        private String endDate;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FactusItem {
        private String name;
        private java.math.BigDecimal quantity;
        private java.math.BigDecimal price;
        private java.math.BigDecimal total;

        @JsonProperty("tax_rate")
        private java.math.BigDecimal taxRate;

        @JsonProperty("tax_amount")
        private java.math.BigDecimal taxAmount;

        @JsonProperty("discount_rate")
        private java.math.BigDecimal discountRate;

        @JsonProperty("code_reference")
        private String codeReference;
    }
}