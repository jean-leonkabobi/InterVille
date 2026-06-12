package com.transport.api.bus.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BusCreateRequest {

    @NotBlank(message = "L'immatriculation est requise")
    private String registration;

    @NotNull(message = "La capacité est requise")
    @Min(value = 1, message = "La capacité doit être au moins 1")
    private Integer capacity;

    private String seatConfig;
}