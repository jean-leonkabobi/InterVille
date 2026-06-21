package com.transport.api.user.dto;

import com.transport.api.user.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProfilClientDto {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}