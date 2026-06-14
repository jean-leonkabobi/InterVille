package com.transport.api.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketValidationRequest {

    @NotBlank(message = "Le QR Code est requis")
    private String qrCode;
}