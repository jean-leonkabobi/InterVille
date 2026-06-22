package com.transport.api.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AgenceAdminDto {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private Long companyId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}