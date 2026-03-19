package com.felipesulez.reto_facturacion.dto.factus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactusPagination {

    private Integer total;

    @JsonProperty("per_page")
    private Integer perPage;

    @JsonProperty("current_page")
    private Integer currentPage;

    @JsonProperty("last_page")
    private Integer lastPage;

    private Integer from;
    private Integer to;
    // links omitidos — no son útiles en el backend
}