package com.transport.api.trajet.dto;

import com.transport.api.trajet.enums.StatutTrajet;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TrajetResultDto {
    private Long id;
    private Long ligneId;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer durationMinutes;
    private Double price;
    private Integer availableSeats;
    private Integer totalSeats;
    private StatutTrajet status;
}