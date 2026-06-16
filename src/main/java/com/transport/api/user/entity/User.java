package com.transport.api.user.entity;

import com.transport.api.common.BaseEntity;
import com.transport.api.user.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "agence_id")
    private Long agenceId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Column(name = "verification_code")
    private String verificationCode;

    @Column(name = "verification_code_expires")
    private LocalDateTime verificationCodeExpires;

    @Column(name = "reset_code")
    private String resetCode;

    @Column(name = "reset_code_expires")
    private LocalDateTime resetCodeExpires;
}