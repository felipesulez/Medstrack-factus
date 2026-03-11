package com.felipesulez.reto_facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "Detalle de los productos o servicios incluidos en la factura")
public class ItemDTO {

    @JsonProperty("code_reference")
    @Schema(example = "SERV-001", description = "Código interno del producto")
    private String codeReference; // Opcional: el Service genera uno si es nulo

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Schema(example = "Consultoría Técnica Medstrack")
    private String name;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad mínima es 0.01")
    @Schema(example = "1")
    private BigDecimal quantity;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", inclusive = false, message = "El precio debe ser mayor a 0")
    @Schema(example = "50000.00")
    private BigDecimal price;

    @JsonProperty("tax_rate")
    @Schema(example = "19.00", description = "Porcentaje del impuesto (Ej: 19.00)")
    private BigDecimal taxRate;

    @JsonProperty("discount_rate")
    @Schema(example = "0.00")
    private BigDecimal discountRate;

    @JsonProperty("unit_measure_id")
    @Schema(example = "70", description = "70 para unidades, verificar tabla de unidades de Factus")
    private Integer unitMeasureId;

    // --- CAMPOS TÉCNICOS PARA EVITAR ERROR 422 ---

    @JsonProperty("is_excluded")
    @Schema(example = "0", description = "0 = No excluido, 1 = Excluido de IVA")
    private Integer isExcluded;

    @JsonProperty("standard_code_id")
    @Schema(example = "1", description = "ID del estándar de codificación (1 para comercial)")
    private Integer standardCodeId;

    @JsonProperty("tribute_id")
    @Schema(example = "1", description = "ID del tributo asociado (1 para IVA)")
    private Integer tributeId;
}