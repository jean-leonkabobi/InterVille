package com.transport.api.reservation.dto;

import com.transport.api.paiement.enums.ModePaiement;
import com.transport.api.reservation.enums.StatutReservation;
import com.transport.api.reservation.enums.StatutTicket;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TicketDetailDto {
    // Infos réservation
    private Long id;
    private String reservationCode;
    private StatutReservation reservationStatus;
    private LocalDateTime reservationDate;
    private Double totalPrice;

    // Infos trajet
    private Long trajetId;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String durationFormatted;

    // Infos sièges
    private List<String> sieges;

    // Infos passager
    private String passengerName;
    private String passengerPhone;
    private String passengerEmail;

    // Infos ticket
    private StatutTicket ticketStatus;
    private String qrCode;
    private String qrCodeImage;
    private LocalDateTime validatedAt;
    private String validatedBy;

    // Infos paiement
    private ModePaiement paymentMode;
    private String paymentReference;
    private LocalDateTime paymentDate;

    private LocalDateTime createdAt;
}