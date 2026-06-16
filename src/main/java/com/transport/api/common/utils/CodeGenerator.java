package com.transport.api.common.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CodeGenerator {

    private static final SecureRandom random = new SecureRandom();

    /**
     * Génère un code à 6 chiffres (ex: 123456)
     */
    public static String generateSixDigitCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Génère un code à 6 chiffres avec timestamp (ex: 123456-1700000000)
     * Pour plus de sécurité
     */
    public static String generateCodeWithTimestamp() {
        String code = generateSixDigitCode();
        long timestamp = System.currentTimeMillis();
        return code + "-" + timestamp;
    }
}