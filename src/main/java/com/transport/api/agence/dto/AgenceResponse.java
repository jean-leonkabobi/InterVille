package com.transport.api.agence.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AgenceResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private Long companyId;
    private LocalDateTime createdAt;
}