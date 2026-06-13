package com.transport.api.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class VerrouillerGroupesRequest {

    @NotNull(message = "L'ID de session est requis")
    private UUID sessionId;

    @NotNull(message = "La liste des sièges est requise")
    private List<String> sieges;
}