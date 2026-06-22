package com.transport.api.sync.dto;

import lombok.Data;

@Data
public class RemboursementPayloadDto {
    private Long reservationId;
    private String motif;
}