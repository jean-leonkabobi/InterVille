package com.transport.api.bus.dto;

import com.transport.api.bus.enums.StatutBus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusDto {
    private Long id;
    private String registration;
    private Integer capacity;
    private String seatConfig;
    private StatutBus status;
    private Long companyId;
}