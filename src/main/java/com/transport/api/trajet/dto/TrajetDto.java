package com.transport.api.trajet.dto;

import com.transport.api.trajet.enums.StatutTrajet;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TrajetDto {
    private Long id;
    private Long ligneId;
    private String ligneName;
    private Long busId;
    private String busRegistration;
    private Long chauffeurId;
    private String chauffeurName;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Double basePrice;
    private StatutTrajet status;
    private Integer availableSeats;
}