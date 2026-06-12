package com.transport.api.auth.dto;

import com.transport.api.user.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private Long id;
    private String email;
    private String fullName;
    private Role role;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
}