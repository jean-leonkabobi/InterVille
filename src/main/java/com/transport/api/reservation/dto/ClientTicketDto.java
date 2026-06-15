package com.transport.api.reservation.dto;

import com.transport.api.reservation.enums.StatutReservation;
import com.transport.api.reservation.enums.StatutTicket;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ClientTicketDto {
    private Long id;
    private String reservationCode;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private List<String> sieges;
    private Double totalPrice;
    private StatutReservation reservationStatus;
    private StatutTicket ticketStatus;
    private String qrCode;
    private LocalDateTime validatedAt;
    private LocalDateTime createdAt;
}