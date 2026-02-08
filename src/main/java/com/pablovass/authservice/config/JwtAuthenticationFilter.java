package com.pablovass.authservice.config;

import com.pablovass.authservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT para validar tokens en cada petición.
 * Extrae el userId del token y lo carga en el SecurityContext.
 * HU-AUTH-04: Perfil Autenticado
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        // Si no hay header o no es Bearer, continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final Long userId = jwtService.extractUserId(jwt);
            final String username = jwtService.extractUsername(jwt);

            // Si el token es válido y no hay autenticación previa
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                if (!jwtService.isTokenExpired(jwt)) {
                    // Crear autenticación con userId como principal
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userId,  // Principal = userId (Long)
                        null,    // Credentials = null (no password)
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("✅ JWT válido para userId: {}, username: {}", userId, username);
                }
            }
        } catch (Exception e) {
            log.error("❌ Error al procesar JWT: {}", e.getMessage());
            // No bloqueamos la petición, Spring Security manejará el 401
        }

        filterChain.doFilter(request, response);
    }
}
