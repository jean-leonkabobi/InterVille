package com.transport.api.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParametresGenerauxDto {
    private Integer delaiAnnulationHeures;      
    private Double penaliteAnnulation;
    private Integer delaiRemboursementJours;
    private Double commissionAgence;
    private Integer timeoutVerrouillageMinutes;
    private String devisePrincipale;
    private String deviseSecondaire;
    private Double tauxConversion;
}