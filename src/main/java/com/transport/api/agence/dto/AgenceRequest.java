package com.transport.api.agence.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgenceRequest {

    @NotBlank(message = "Le nom de l'agence est requis")
    private String name;

    private String address;
    private String phone;
}