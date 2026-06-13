package com.transport.api.reservation.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SiegesDisponiblesResponse {
    private Long trajetId;
    private Integer totalSieges;
    private Integer siegesLibres;
    private Integer siegesVerrouilles;
    private Integer siegesReserves;
    private List<SiegeDisponibleDto> sieges;
}