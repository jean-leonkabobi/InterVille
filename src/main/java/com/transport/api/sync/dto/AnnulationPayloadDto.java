package com.transport.api.sync.dto;

import lombok.Data;

@Data
public class AnnulationPayloadDto {
    private Long reservationId;
    private String motif;
}