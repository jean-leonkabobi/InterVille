package com.transport.api.auth.dto;

import com.transport.api.user.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileDto {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private Long agenceId;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}