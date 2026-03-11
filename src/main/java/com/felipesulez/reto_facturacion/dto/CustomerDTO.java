package com.felipesulez.reto_facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CustomerDTO {

    @NotBlank(message = "La identificación es obligatoria")
    private String identification;

    // Se quita @NotBlank para que el Service lo calcule
    private String dv;

    @NotBlank(message = "La razón social (company) es obligatoria")
    private String company;

    @NotBlank(message = "El nombre es obligatorio")
    private String names;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo no es válido")
    private String email;

    @JsonProperty("identification_document_id")
    private String identificationDocumentId; // Se llena en el Service (default "3" o "6")

    @JsonProperty("legal_organization_id")
    private String legalOrganizationId; // Se llena en el Service

    @JsonProperty("tribute_id")
    private String tributeId; // Se llena en el Service

    @JsonProperty("municipality_id")
    private String municipalityId; // Se llena en el Service
}