package com.transport.api.ticket.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TicketPrintDto {
    private String companyName;
    private String companyAddress;
    private String companyPhone;

    private String reservationCode;
    private String passengerName;
    private String passengerPhone;
    private String seatNumber;

    private String departureCity;
    private String arrivalCity;
    private String departureTime;
    private String arrivalTime;
    private String duration;

    private String price;
    private String currency;
    private String paymentMethod;
    private String paymentReference;

    private String qrCodeBase64;
    private String qrCodeText;

    private String printedAt;
    private String agentName;
}