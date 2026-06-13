package com.transport.api.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class VerrouillerSiegeRequest {

    @NotNull(message = "L'ID de session est requis")
    private UUID sessionId;
}