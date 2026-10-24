package com.smartpoweron.gestion_materiales.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Proveedor de tokens JWT (RNF03).
 * Firma tokens HS256 con una clave secreta configurada en app.jwt.secret.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-minutes:480}")
    private long jwtExpirationMinutes;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Si la clave viene en Base64, la decodifica; si no, la usa como UTF-8.
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            keyBytes = jwtSecret.getBytes();
        }
        // HS256 necesita al menos 256 bits (32 bytes).
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT inicializado. Expiración por defecto: {} minutos", jwtExpirationMinutes);
    }

    /**
     * Genera un token JWT firmado con HS256 para un usuario autenticado.
     */
    public String generarToken(Authentication authentication) {
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return generarToken(principal.getUsername(),
                principal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
    }

    /**
     * Genera un token JWT a partir del correo y la lista de roles.
     */
    public String generarToken(String correo, List<String> roles) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + (jwtExpirationMinutes * 60_000));

        return Jwts.builder()
                .subject(correo)
                .claim("roles", roles)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Obtiene el nombre de usuario (correo) desde un token JWT.
     */
    public String getCorreoFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Valida la firma y la expiración del token.
     * ⚠️ IMPORTANTE: El orden de los catch es de la excepción más específica 
     * a la más general para evitar errores de compilación en Java.
     */
    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 1. Primero las excepciones específicas (hijas de JwtException)
            log.warn("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            // 2. Otras excepciones específicas
            log.warn("Token JWT no soportado: {}", e.getMessage());
        } catch (JwtException e) {
            // 3. Luego la excepción general (padre) como red de seguridad
            log.warn("Token JWT inválido (firma o formato): {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            // 4. Excepción de Java estándar para argumentos nulos/vacíos
            log.warn("Token JWT vacío o malformado: {}", e.getMessage());
        }
        return false;
    }

    public long getExpiracionMinutes() {
        return jwtExpirationMinutes;
    }
}