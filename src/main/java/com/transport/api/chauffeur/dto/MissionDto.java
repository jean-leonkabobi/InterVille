package com.transport.api.chauffeur.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MissionDto {
    private Long trajetId;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String busRegistration;
    private Integer totalPlaces;
    private Integer placesReservees;
    private Integer placesDisponibles;
    private String statut; // SCHEDULED, DEPARTED, ARRIVED, CANCELLED
    private String ligneName;
}