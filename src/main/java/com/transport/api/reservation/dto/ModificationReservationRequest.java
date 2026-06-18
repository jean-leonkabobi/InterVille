package com.transport.api.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ModificationReservationRequest {
    private String passengerName;
    private String passengerPhone;
    private List<String> numerosSieges;  // Nouveaux sièges (si changement)
}