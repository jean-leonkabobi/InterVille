package com.transport.api.reservation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketValidationResponse {
    private boolean valid;
    private String message;
    private String passengerName;
    private String departureCity;
    private String arrivalCity;
    private String departureTime;
    private String seatNumber;
}