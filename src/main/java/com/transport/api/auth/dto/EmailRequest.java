package com.transport.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailRequest {
    @NotBlank(message = "L'email est requis")
    @Email(message = "Format email invalide")
    private String email;
}