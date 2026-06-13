package com.transport.api.reservation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SiegeDisponibleDto {
    private Long siegeId;
    private String numero;
    private String type;
    private String statut; // LIBRE, VERROUILLE, RESERVE
    private LocalDateTime verrouExpireAt;
}