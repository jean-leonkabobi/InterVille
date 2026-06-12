package com.transport.api.context;

import com.transport.api.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Ignorer les endpoints d'authentification
        if (request.getRequestURI().startsWith("/auth/")) {
            TenantContext.clear();
            return true;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            try {
                Long companyId = jwtService.extractCompanyId(jwt);
                TenantContext.setCurrentTenant(companyId);
            } catch (Exception e) {
                TenantContext.clear();
            }
        } else {
            TenantContext.clear();
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}