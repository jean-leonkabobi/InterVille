package com.transport.api.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RapportVentesLigneDto {
    private Long ligneId;
    private String departureCity;
    private String arrivalCity;
    private Long totalReservations;
    private Long totalPassagers;
    private Double montantTotal;
    private Double montantMoyen;
    private Double tauxRemplissage;
}