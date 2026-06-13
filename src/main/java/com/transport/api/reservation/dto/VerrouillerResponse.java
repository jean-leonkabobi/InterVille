package com.transport.api.reservation.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VerrouillerResponse {
    private boolean success;
    private String message;
    private List<String> siegesVerrouilles;
    private List<String> siegesIndisponibles;
    private LocalDateTime expiresAt;
}