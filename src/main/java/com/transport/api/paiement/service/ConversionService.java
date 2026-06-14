package com.transport.api.paiement.service;

import com.transport.api.paiement.enums.Devise;
import org.springframework.stereotype.Service;

@Service
public class ConversionService {

    // Taux de conversion fixe (1 USD = 2800 CDF)
    // À rendre configurable plus tard
    private static final double USD_TO_CDF_RATE = 2800.0;

    public double convertirEnCDF(double montant, Devise devise) {
        if (devise == Devise.CDF) {
            return montant;
        } else if (devise == Devise.USD) {
            return montant * USD_TO_CDF_RATE;
        }
        throw new IllegalArgumentException("Devise non supportée: " + devise);
    }

    public double convertirDeCDF(double montantCDF, Devise devise) {
        if (devise == Devise.CDF) {
            return montantCDF;
        } else if (devise == Devise.USD) {
            return montantCDF / USD_TO_CDF_RATE;
        }
        throw new IllegalArgumentException("Devise non supportée: " + devise);
    }
}