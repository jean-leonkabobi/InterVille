package com.transport.api.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TableauBordGlobalDto {
    private LocalDateTime date;

    // Indicateurs globaux
    private Long totalUtilisateurs;
    private Long totalAgences;
    private Long totalBus;
    private Long totalTrajets;

    // Ventes
    private Double revenusTotal;
    private Double revenusAujourdhui;
    private Double revenusMois;
    private Double revenusAnnee;

    // Réservations
    private Long reservationsTotal;
    private Long reservationsAujourdhui;
    private Long reservationsMois;
    private Long reservationsAnnee;

    // Taux de remplissage moyen
    private Double tauxRemplissageMoyen;

    // Incidents
    private Long incidentsOpen;
    private Long incidentsInProgress;
    private Long incidentsResolved;
    private Long incidentsTotal;

    // Top agences
    private List<TopAgenceDto> topAgences;
    // Top lignes
    private List<TopLigneDto> topLignes;

    @Data
    @Builder
    public static class TopAgenceDto {
        private Long agenceId;
        private String agenceNom;
        private Double chiffreAffaires;
        private Long reservationsCount;
    }

    @Data
    @Builder
    public static class TopLigneDto {
        private Long ligneId;
        private String departureCity;
        private String arrivalCity;
        private Long reservationsCount;
        private Double chiffreAffaires;
    }
}