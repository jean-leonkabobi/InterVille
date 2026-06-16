package com.transport.api.auth.service;

import com.transport.api.auth.dto.*;
import com.transport.api.auth.entity.RefreshToken;
import com.transport.api.auth.repository.RefreshTokenRepository;
import com.transport.api.common.utils.CodeGenerator;
import com.transport.api.company.entity.Company;
import com.transport.api.company.repository.CompanyRepository;
import com.transport.api.user.entity.User;
import com.transport.api.user.enums.Role;
import com.transport.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ResendEmailService resendEmailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.jwt.access-expiration:900000}")
    private long accessExpiration;

    @Value("${app.jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    private static final int CODE_EXPIRATION_MINUTES = 15;

    /**
     * 1. Inscription - Création du compte + envoi code vérification
     */
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        Company company = companyRepository.findById(1L)
                .orElseGet(() -> {
                    Company newCompany = new Company();
                    newCompany.setName("Transport RDC");
                    newCompany.setCompanyId(1L);
                    return companyRepository.save(newCompany);
                });

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : Role.CLIENT);
        user.setAgenceId(request.getAgenceId());
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setCompanyId(company.getId());

        // Générer code à 6 chiffres
        String verificationCode = CodeGenerator.generateSixDigitCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpires(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));

        userRepository.save(user);

        // Envoyer code par email
        resendEmailService.sendVerificationCode(user.getEmail(), verificationCode);

        return "Inscription réussie. Un code de vérification à 6 chiffres vous a été envoyé par email.";
    }

    /**
     * 2. Vérification email avec code
     */
    @Transactional
    public String verifyEmail(VerifyCodeRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email déjà vérifié");
        }

        if (!request.getCode().equals(user.getVerificationCode())) {
            throw new RuntimeException("Code de vérification invalide");
        }

        if (user.getVerificationCodeExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code de vérification expiré");
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpires(null);
        userRepository.save(user);

        return "Email vérifié avec succès. Vous pouvez maintenant vous connecter.";
    }

    /**
     * 3. Renvoyer code de vérification
     */
    @Transactional
    public String resendVerification(EmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email déjà vérifié");
        }

        String newCode = CodeGenerator.generateSixDigitCode();
        user.setVerificationCode(newCode);
        user.setVerificationCodeExpires(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
        userRepository.save(user);

        resendEmailService.sendVerificationCode(user.getEmail(), newCode);

        return "Nouveau code de vérification envoyé par email.";
    }

    /**
     * 4. Connexion
     */
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.getIsActive()) {
            throw new RuntimeException("Compte désactivé");
        }

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Veuillez vérifier votre email avant de vous connecter");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails, user.getId(), user.getCompanyId());
        String refreshToken = generateRefreshToken(user);

        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessExpiration)
                .build();
    }

    /**
     * 5. Déconnexion
     */
    @Transactional
    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    /**
     * 6. Refresh token
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token invalide"));

        if (refreshToken.getRevoked()) {
            throw new RuntimeException("Refresh token révoqué");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expiré");
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtService.generateToken(userDetails, user.getId(), user.getCompanyId());

        String newRefreshToken = generateRefreshToken(user);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(accessExpiration)
                .build();
    }

    /**
     * 7. Profil utilisateur
     */
    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return UserProfileDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .agenceId(user.getAgenceId())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 8. Mot de passe oublié - envoi code
     */
    @Transactional
    public String forgotPassword(EmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String resetCode = CodeGenerator.generateSixDigitCode();
        user.setResetCode(resetCode);
        user.setResetCodeExpires(LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES));
        userRepository.save(user);

        resendEmailService.sendPasswordResetCode(user.getEmail(), resetCode);

        return "Un code de réinitialisation à 6 chiffres vous a été envoyé par email.";
    }

    /**
     * 9. Réinitialisation du mot de passe avec code
     */
    @Transactional
    public String resetPassword(ResetPasswordCodeRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!request.getCode().equals(user.getResetCode())) {
            throw new RuntimeException("Code de réinitialisation invalide");
        }

        if (user.getResetCodeExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code de réinitialisation expiré");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetCode(null);
        user.setResetCodeExpires(null);
        userRepository.save(user);

        // Invalider tous les refresh tokens
        refreshTokenRepository.deleteByUserId(user.getId());

        return "Mot de passe réinitialisé avec succès.";
    }

    private String generateRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
        return token;
    }
}