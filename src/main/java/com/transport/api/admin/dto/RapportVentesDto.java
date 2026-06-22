package com.transport.api.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RapportVentesDto {
    private String periode;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Long totalReservations;
    private Long totalPassagers;
    private Double montantTotal;
    private Double montantMoyen;
    private Double montantMin;
    private Double montantMax;
    private String devise;
}