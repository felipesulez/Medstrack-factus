package com.felipesulez.reto_facturacion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CustomerDTO {
    @JsonProperty("identification")
    private String identification;

    @JsonProperty("dv")
    private String dv;

    @JsonProperty("company")
    private String company;

    @JsonProperty("trade_name")
    private String tradeName;

    @JsonProperty("names")
    private String names;

    @JsonProperty("address")
    private String address;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("identification_document_id")
    private String identificationDocumentId = "3";

    @JsonProperty("legal_organization_id")
    private String legalOrganizationId = "2";

    @JsonProperty("tribute_id")
    private String tributeId = "21";

    @JsonProperty("municipality_id")
    private String municipalityId = "980";
}