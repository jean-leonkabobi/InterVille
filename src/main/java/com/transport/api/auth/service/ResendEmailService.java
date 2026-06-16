package com.transport.api.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ResendEmailService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${resend.api-key:}")
    private String apiKey;

    @Value("${resend.from-email:noreply@transport-rdc.com}")
    private String fromEmail;

    private static final String RESEND_API_URL = "https://api.resend.com";

    /**
     * Envoie un code de vérification à 6 chiffres par email
     */
    public void sendVerificationCode(String to, String code) {
        String subject = "📱 Votre code de vérification - Transport RDC";
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Transport RDC</h2>
                    <p>Merci de vous être inscrit ! Voici votre code de vérification :</p>
                    <div style="font-size: 32px; font-weight: bold; text-align: center; padding: 20px; background-color: #f0f0f0; border-radius: 10px; letter-spacing: 5px;">
                        %s
                    </div>
                    <p style="margin-top: 20px; color: #7f8c8d; font-size: 12px;">Ce code expire dans 15 minutes. Ne le partagez avec personne.</p>
                    <p style="color: #7f8c8d; font-size: 12px;">Si vous n'avez pas créé de compte, ignorez cet email.</p>
                </div>
            </body>
            </html>
            """, code);

        sendEmail(to, subject, htmlContent);
    }

    /**
     * Envoie un code de réinitialisation de mot de passe à 6 chiffres par email
     */
    public void sendPasswordResetCode(String to, String code) {
        String subject = "🔐 Réinitialisation de votre mot de passe - Transport RDC";
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c3e50;">Transport RDC</h2>
                    <p>Vous avez demandé la réinitialisation de votre mot de passe. Voici votre code :</p>
                    <div style="font-size: 32px; font-weight: bold; text-align: center; padding: 20px; background-color: #f0f0f0; border-radius: 10px; letter-spacing: 5px;">
                        %s
                    </div>
                    <p style="margin-top: 20px; color: #7f8c8d; font-size: 12px;">Ce code expire dans 15 minutes. Ne le partagez avec personne.</p>
                    <p style="color: #7f8c8d; font-size: 12px;">Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>
                </div>
            </body>
            </html>
            """, code);

        sendEmail(to, subject, htmlContent);
    }

    /**
     * Envoi d'email via l'API Resend
     */
    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(RESEND_API_URL).build();

            // Nettoyer le HTML pour JSON (échapper les guillemets et retours à la ligne)
            String cleanedHtml = htmlContent
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");

            // Construire le payload JSON
            String payload = String.format("""
                {
                    "from": "%s",
                    "to": ["%s"],
                    "subject": "%s",
                    "html": "%s"
                }
                """, fromEmail, to, subject, cleanedHtml);

            // Appel API Resend
            String response = webClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            error -> error.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        System.err.println("Erreur Resend: " + errorBody);
                                        return Mono.error(new RuntimeException("Erreur lors de l'envoi de l'email: " + errorBody));
                                    }))
                    .bodyToMono(String.class)
                    .block();

            System.out.println("✅ Email envoyé avec succès à " + to);
            System.out.println("Réponse Resend: " + response);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de l'email: " + e.getMessage());
            // On ne lance pas d'exception pour ne pas bloquer l'inscription
            // Mais on log l'erreur
        }
    }
}