package com.transport.api.reservation.dto;

import com.transport.api.reservation.enums.StatutTicket;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketDto {
    private Long id;
    private Long reservationId;
    private String qrCode;
    private String qrCodeImage;
    private LocalDateTime validatedAt;
    private Long validatedBy;
    private StatutTicket status;
    private LocalDateTime createdAt;
}