package com.transport.api.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailtrapEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envoie un code de vérification à 6 chiffres par email via Mailtrap
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
     * Envoie un code de réinitialisation de mot de passe à 6 chiffres par email via Mailtrap
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
     * Envoi d'email via SMTP (Mailtrap)
     */
    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("✅ Email envoyé avec succès à {}", to);
            log.info("   📬 Consultez votre boîte Mailtrap pour voir le message");

        } catch (MessagingException e) {
            log.error("❌ Erreur lors de l'envoi de l'email: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }
}