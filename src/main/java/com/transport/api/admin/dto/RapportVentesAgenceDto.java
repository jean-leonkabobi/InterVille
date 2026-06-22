package com.transport.api.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RapportVentesAgenceDto {
    private Long agenceId;
    private String agenceNom;
    private Long totalReservations;
    private Long totalPassagers;
    private Double montantTotal;
    private Double montantMoyen;
}