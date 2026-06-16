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

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-email}")
    private String fromEmail;

    private static final String VERIFICATION_TEMPLATE = """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"></head>
        <body style="font-family: Arial, sans-serif;">
            <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2 style="color: #2c3e50;">Bienvenue sur Transport RDC</h2>
                <p>Merci de vous être inscrit ! Veuillez vérifier votre adresse email en cliquant sur le lien ci-dessous :</p>
                <a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #3498db; color: white; text-decoration: none; border-radius: 5px;">Vérifier mon email</a>
                <p style="margin-top: 20px; color: #7f8c8d; font-size: 12px;">Ce lien expire dans 24 heures.</p>
            </div>
        </body>
        </html>
    """;

    private static final String RESET_TEMPLATE = """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"></head>
        <body style="font-family: Arial, sans-serif;">
            <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2 style="color: #2c3e50;">Réinitialisation de votre mot de passe</h2>
                <p>Vous avez demandé la réinitialisation de votre mot de passe. Cliquez sur le lien ci-dessous :</p>
                <a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #e74c3c; color: white; text-decoration: none; border-radius: 5px;">Réinitialiser mon mot de passe</a>
                <p style="margin-top: 20px; color: #7f8c8d; font-size: 12px;">Ce lien expire dans 1 heure.</p>
            </div>
        </body>
        </html>
    """;

    public void sendVerificationEmail(String to, String token, String baseUrl) {
        String verificationUrl = baseUrl + "/auth/verify-email?token=" + token;
        String htmlContent = String.format(VERIFICATION_TEMPLATE, verificationUrl);

        sendEmail(to, "Vérifiez votre email - Transport RDC", htmlContent);
    }

    public void sendPasswordResetEmail(String to, String token, String baseUrl) {
        String resetUrl = baseUrl + "/auth/reset-password?token=" + token;
        String htmlContent = String.format(RESET_TEMPLATE, resetUrl);

        sendEmail(to, "Réinitialisation de votre mot de passe - Transport RDC", htmlContent);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            WebClient webClient = webClientBuilder.baseUrl("https://api.resend.com").build();

            // Créer le payload JSON
            var payload = String.format("""
                {
                    "from": "%s",
                    "to": ["%s"],
                    "subject": "%s",
                    "html": "%s"
                }
            """, fromEmail, to, subject, escapeJson(htmlContent));

            webClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("Erreur Resend: " + error))))
                    .bodyToMono(String.class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email: " + e.getMessage(), e);
        }
    }

    private String escapeJson(String json) {
        return json.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}