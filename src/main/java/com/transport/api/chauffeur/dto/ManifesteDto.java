package com.transport.api.chauffeur.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ManifesteDto {
    private Long trajetId;
    private String departureCity;
    private String arrivalCity;
    private String departureTime;
    private String arrivalTime;
    private String busRegistration;
    private Integer totalPassagers;
    private List<PassagerDto> passagers;

    @Data
    @Builder
    public static class PassagerDto {
        private String nom;
        private String telephone;
        private String siege;
        private Boolean embarque;
        private String qrCode;
    }
}