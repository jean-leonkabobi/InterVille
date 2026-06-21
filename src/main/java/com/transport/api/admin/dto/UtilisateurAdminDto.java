package com.transport.api.admin.dto;

import com.transport.api.user.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UtilisateurAdminDto {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private Long agenceId;
    private String agenceNom;
    private Boolean isActive;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}