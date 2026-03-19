package com.felipesulez.reto_facturacion.dto.factus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FactusRelatedDocument {
    private Integer id;
    private String number;
}