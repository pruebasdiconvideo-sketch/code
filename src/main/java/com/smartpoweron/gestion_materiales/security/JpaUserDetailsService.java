package com.smartpoweron.gestion_materiales.security;

import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de UserDetailsService basada en JPA (RNF03, RNF04).
 * <p>
 * Carga al usuario por su correo electrónico desde la BD MySQL y construye
 * un UserDetails con su rol como autoridad (formato ROLE_&lt;NOMBRE&gt;).
 * Las contraseñas se comparan con BCryptPasswordEncoder (rounds=10).
 *
 * @author Grupo 8 - UTP
 */
@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con correo: " + correo));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new UsernameNotFoundException("El usuario está inactivo");
        }

        String role = "ROLE_" + usuario.getRol().getNombre();
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        // Actualizar último acceso (RNF09 auditabilidad)
        // — se hace en el AuthService para evitar writes en consulta de solo lectura

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getPasswordHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
