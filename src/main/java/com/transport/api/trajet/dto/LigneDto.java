package com.transport.api.trajet.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LigneDto {
    private Long id;
    private String departureCity;
    private String arrivalCity;
    private Integer durationMinutes;
    private Boolean isActive;
    private Long companyId;
}