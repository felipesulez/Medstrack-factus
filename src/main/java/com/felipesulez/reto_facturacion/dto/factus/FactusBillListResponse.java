package com.felipesulez.reto_facturacion.dto.factus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Respuesta paginada del listado de facturas")
public class FactusBillListResponse {

    private String status;
    private String message;
    private FactusBillListData data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Contenedor con array de facturas y paginación")
    public static class FactusBillListData {

        // Factus usa data.data[] — doble anidamiento intencional de su API
        private List<FactusBillSummary> data;
        private FactusPagination pagination;
    }
}