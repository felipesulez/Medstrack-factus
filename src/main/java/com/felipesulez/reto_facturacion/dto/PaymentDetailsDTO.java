package com.felipesulez.reto_facturacion.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailsDTO {
    private String code;

    @JsonValue // <--- ESTA ES LA MAGIA
    public String getCode() {
        return code;
    }
}