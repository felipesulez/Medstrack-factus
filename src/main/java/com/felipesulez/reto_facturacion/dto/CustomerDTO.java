package com.felipesulez.reto_facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Datos del cliente receptor de la factura")
public class CustomerDTO {

    @NotBlank(message = "La identificación es obligatoria")
    @Schema(
            description = "NIT (sin dígito verificador) o cédula del cliente.",
            example = "123456789"
    )
    private String identification;

    @Schema(
            description = "Dígito de verificación del NIT. Se calcula automáticamente si identification_document_id es '6' (NIT).",
            example = "9",
            nullable = true
    )
    private String dv;

    @NotBlank(message = "La razón social (company) es obligatoria")
    @Schema(
            description = "Razón social o nombre de la empresa.",
            example = "Medstrack SAS"
    )
    private String company;

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(
            description = "Nombre completo del representante o persona natural.",
            example = "Felipe Sulez"
    )
    private String names;

    @NotBlank(message = "La dirección es obligatoria")
    @Schema(
            description = "Dirección fiscal del cliente.",
            example = "Calle 5 # 2-10, La Pamba"
    )
    private String address;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @Schema(
            description = "Correo electrónico al que Factus enviará la factura.",
            example = "contacto@medstrack.com.co"
    )
    private String email;

    @JsonProperty("identification_document_id")
    @Schema(
            description = "Tipo de documento. Opcional — el sistema lo infiere por la longitud del NIT. '3'=Cédula, '6'=NIT.",
            example = "3",
            nullable = true,
            allowableValues = {"3", "6"}
    )
    private String identificationDocumentId;

    @JsonProperty("legal_organization_id")
    @Schema(
            description = "Tipo de organización legal. Opcional — por defecto '1' (Persona Jurídica). '1'=Jurídica, '2'=Natural.",
            example = "2",
            nullable = true,
            allowableValues = {"1", "2"}
    )
    private String legalOrganizationId;

    @JsonProperty("tribute_id")
    @Schema(
            description = "Responsabilidad tributaria. Opcional — por defecto '21' (No responsable de IVA).",
            example = "21",
            nullable = true
    )
    private String tributeId;

    @JsonProperty("municipality_id")
    @Schema(
            description = "ID del municipio Factus. Opcional — por defecto '980' (San Gil). Ver tabla de municipios.",
            example = "980",
            nullable = true
    )
    private String municipalityId;
}