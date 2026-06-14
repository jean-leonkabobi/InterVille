package com.transport.api.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReservationRequest {

    @NotNull(message = "L'ID du trajet est requis")
    private Long trajetId;

    @NotNull(message = "L'ID de session est requis")
    private UUID sessionId;

    @NotNull(message = "La liste des sièges est requise")
    private List<String> numerosSieges;

    private String passengerName;
    private String passengerPhone;
    private String passengerEmail;
}