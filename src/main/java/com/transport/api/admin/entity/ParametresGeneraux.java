package com.transport.api.admin.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "parametres_generaux")
@Getter
@Setter
public class ParametresGeneraux {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delai_annulation_heures")
    private Integer delaiAnnulationHeures;

    @Column(name = "penalite_annulation")
    private Double penaliteAnnulation;

    @Column(name = "delai_remboursement_jours")
    private Integer delaiRemboursementJours;

    @Column(name = "commission_agence")
    private Double commissionAgence;

    @Column(name = "timeout_verrouillage_minutes")
    private Integer timeoutVerrouillageMinutes;

    @Column(name = "devise_principale", length = 10)
    private String devisePrincipale;

    @Column(name = "devise_secondaire", length = 10)
    private String deviseSecondaire;

    @Column(name = "taux_conversion")
    private Double tauxConversion;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}