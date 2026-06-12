package com.transport.api.auth.service;

import com.transport.api.auth.dto.AuthResponse;
import com.transport.api.auth.dto.LoginRequest;
import com.transport.api.auth.dto.RegisterRequest;
import com.transport.api.company.entity.Company;
import com.transport.api.company.repository.CompanyRepository;
import com.transport.api.user.entity.User;
import com.transport.api.user.enums.Role;
import com.transport.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }

        // En V1, on utilise la première compagnie (id=1)
        Company company = companyRepository.findById(1L)
                .orElseGet(() -> {
                    Company defaultCompany = new Company();
                    defaultCompany.setName("Transport Company");
                    defaultCompany.setCompanyId(1L); // Auto-référence pour V1
                    return companyRepository.save(defaultCompany);
                });

        // Vérifier l'agence_id si rôle AGENT
        if (request.getRole() == Role.AGENT && request.getAgenceId() == null) {
            throw new RuntimeException("L'agence est requise pour un agent");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setAgenceId(request.getAgenceId());
        user.setIsActive(true);
        user.setCompanyId(company.getId());

        User savedUser = userRepository.save(user);

        // Générer les tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtService.generateToken(userDetails, savedUser.getId(), savedUser.getCompanyId());
        String refreshToken = jwtService.generateRefreshToken(userDetails, savedUser.getId(), savedUser.getCompanyId());

        return AuthResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(86400000L)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.getIsActive()) {
            throw new RuntimeException("Compte désactivé");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails, user.getId(), user.getCompanyId());
        String refreshToken = jwtService.generateRefreshToken(userDetails, user.getId(), user.getCompanyId());

        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(86400000L)
                .build();
    }
}