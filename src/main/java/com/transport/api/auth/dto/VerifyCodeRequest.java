package com.transport.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyCodeRequest {

    @NotBlank(message = "L'email est requis")
    @Email(message = "Format email invalide")
    private String email;

    @NotBlank(message = "Le code est requis")
    @Pattern(regexp = "\\d{6}", message = "Le code doit contenir exactement 6 chiffres")
    private String code;
}