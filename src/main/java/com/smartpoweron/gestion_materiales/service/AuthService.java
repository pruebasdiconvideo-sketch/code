package com.smartpoweron.gestion_materiales.service;

import com.smartpoweron.gestion_materiales.dto.LoginRequest;
import com.smartpoweron.gestion_materiales.dto.LoginResponse;
import com.smartpoweron.gestion_materiales.exception.ReglaNegocioException;
import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.repository.UsuarioRepository;
import com.smartpoweron.gestion_materiales.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de autenticación (RF01, RF04, RNF03, RNF04).
 * <p>
 * Valida correo y contraseña contra la BD MySQL usando BCrypt y produce un
 * token JWT firmado HS256 con expiración configurable (RNF03).
 *
 * @author Grupo 8 - UTP
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;

    /**
     * Autentica al usuario y devuelve el token JWT (RF01).
     * <p>
     * Mensaje estándar ante credenciales inválidas: "Usuario o contraseña incorrectos".
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Intento de login para correo: {}", request.getCorreo());

        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword());
            Authentication authentication = authenticationManager.authenticate(authToken);

            UserDetails principal = (UserDetails) authentication.getPrincipal();
            String jwt = jwtTokenProvider.generarToken(authentication);

            // Actualizar último acceso (RNF09 auditabilidad)
            Usuario usuario = usuarioRepository.findByCorreo(principal.getUsername())
                    .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado"));
            usuarioRepository.actualizarUltimoAcceso(usuario.getIdUsuario(), LocalDateTime.now());

            log.info("Login exitoso para {} con rol {}",
                    usuario.getCorreo(), usuario.getRol().getNombre());

            return LoginResponse.builder()
                    .token(jwt)
                    .tipo("Bearer")
                    .idUsuario(usuario.getIdUsuario())
                    .nombreCompleto(usuario.getNombreCompleto())
                    .correo(usuario.getCorreo())
                    .rol(usuario.getRol().getNombre())
                    .expiraEn(jwtTokenProvider.getExpiracionMinutes() * 60_000L)
                    .build();
        } catch (BadCredentialsException e) {
            log.warn("Login fallido para {}: credenciales inválidas", request.getCorreo());
            throw new ReglaNegocioException(
                    "Usuario o contraseña incorrectos", "AUTH-001");
        }
    }

    /**
     * Verifica que el token JWT sea válido.
     */
    public boolean validarToken(String token) {
        return jwtTokenProvider.validarToken(token);
    }

    /**
     * Lista de roles del usuario autenticado (para el frontend).
     */
    public List<String> obtenerRolesDeToken(String token) {
        String correo = jwtTokenProvider.getCorreoFromToken(token);
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado en token"));
        return List.of("ROLE_" + usuario.getRol().getNombre());
    }
}
