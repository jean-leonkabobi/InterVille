package com.transport.api.agence.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TableauBordAgenceDto {
    private Long agenceId;
    private String agenceNom;
    private LocalDateTime date;

    // Indicateurs du jour
    private Integer ventesDuJour;
    private Double montantTotalDuJour;
    private Integer reservationsDuJour;
    private Integer passagersDuJour;
    private Double tauxRemplissageMoyen;

    // Prochains départs
    private List<ProchainDepartDto> prochainsDeparts;

    // Résumés
    private ResumesDto resumes;

    @Data
    @Builder
    public static class ProchainDepartDto {
        private Long trajetId;
        private String departureCity;
        private String arrivalCity;
        private LocalDateTime departureTime;
        private Integer placesDisponibles;
        private Integer totalPlaces;
        private Double tauxRemplissage;
    }

    @Data
    @Builder
    public static class ResumesDto {
        private Integer totalVentesMois;
        private Double montantTotalMois;
        private Integer totalVentesSemaine;
        private Double montantTotalSemaine;
    }
}