package com.transport.api.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReservationAgentRequest {

    @NotNull(message = "L'ID du trajet est requis")
    private Long trajetId;

    @NotNull(message = "La liste des sièges est requise")
    private List<String> numerosSieges;

    @NotBlank(message = "Le nom du passager est requis")
    private String passengerName;

    @NotBlank(message = "Le téléphone du passager est requis")
    private String passengerPhone;

    private String passengerEmail;
    private Integer bagages;
    private Boolean imprimerTicket = true;
}