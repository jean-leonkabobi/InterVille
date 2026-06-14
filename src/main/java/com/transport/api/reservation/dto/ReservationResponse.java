package com.transport.api.reservation.dto;

import com.transport.api.reservation.enums.StatutReservation;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ReservationResponse {
    private Long id;
    private UUID reservationCode;
    private Long trajetId;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private List<String> sieges;
    private BigDecimal totalPrice;
    private StatutReservation status;
    private LocalDateTime expiresAt;
    private String passengerName;
    private String passengerPhone;
}