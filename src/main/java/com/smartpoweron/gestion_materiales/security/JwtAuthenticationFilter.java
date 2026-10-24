package com.smartpoweron.gestion_materiales.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que intercepta cada petición HTTP, extrae el token JWT del header
 * Authorization, valida la firma HS256 y carga el usuario en el contexto de
 * seguridad (RNF03).
 * <p>
 * Solo se aplica a rutas /api/** (stateless). Las vistas Thymeleaf mantienen
 * sesión HTTP tradicional gestionada por SecurityConfig.
 *
 * @author Grupo 8 - UTP
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final JpaUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extraerToken(request);
        if (StringUtils.hasText(token) && tokenProvider.validarToken(token)) {
            String correo = tokenProvider.getCorreoFromToken(token);
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(correo);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null,
                                userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("JWT autenticado para {} en {}", correo, request.getRequestURI());
            } catch (Exception e) {
                log.warn("Fallo al cargar usuario desde JWT: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token del header Authorization: Bearer &lt;token&gt;.
     */
    private String extraerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Solo procesa rutas /api/** (stateless). Otras rutas se manejan con sesión.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/");
    }
}
