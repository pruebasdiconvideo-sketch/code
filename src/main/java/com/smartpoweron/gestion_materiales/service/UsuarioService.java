package com.smartpoweron.gestion_materiales.service;

import com.smartpoweron.gestion_materiales.exception.ReglaNegocioException;
import com.smartpoweron.gestion_materiales.model.Rol;
import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.repository.RolRepository;
import com.smartpoweron.gestion_materiales.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de gestión de usuarios (RF03, OE6).
 * <p>
 * Permite al Administrador registrar, editar y desactivar usuarios, asignándoles
 * un rol de los cinco definidos. Las contraseñas se almacenan como hash BCrypt.
 * 
 * @author Grupo 8 - UTP
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lista todos los usuarios (RF03 - parte de gestión de usuarios).
     */
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * Obtiene un usuario por ID.
     */
    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("Usuario no encontrado", "USR-404"));
    }

    /**
     * Crea un nuevo usuario (RF03, P1-RN02 correo único).
     * La contraseña inicial se cifra con BCrypt (RNF04).
     */
    @Transactional
    public Usuario crearUsuario(Usuario usuario, String passwordInicial) {
        if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new ReglaNegocioException(
                    "El correo ya se encuentra registrado", "USR-001");
        }
        if (usuario.getRol() == null || usuario.getRol().getIdRol() == null) {
            throw new ReglaNegocioException("El rol es obligatorio", "USR-002");
        }
        Rol rol = rolRepository.findById(usuario.getRol().getIdRol())
                .orElseThrow(() -> new ReglaNegocioException("Rol no encontrado", "ROL-404"));
        usuario.setRol(rol);
        usuario.setPasswordHash(passwordEncoder.encode(passwordInicial));
        usuario.setActivo(true);
        return usuarioRepository.save(usuario);
    }

    /**
     * Actualiza un usuario existente (RF03). No permite cambiar el password aquí.
     */
    @Transactional
    public Usuario actualizarUsuario(Long id, Usuario datos) {
        Usuario existente = obtenerPorId(id);
        existente.setNombreCompleto(datos.getNombreCompleto());
        existente.setCorreo(datos.getCorreo());
        if (datos.getRol() != null && datos.getRol().getIdRol() != null) {
            Rol rol = rolRepository.findById(datos.getRol().getIdRol())
                    .orElseThrow(() -> new ReglaNegocioException("Rol no encontrado", "ROL-404"));
            existente.setRol(rol);
        }
        if (datos.getActivo() != null) {
            existente.setActivo(datos.getActivo());
        }
        return usuarioRepository.save(existente);
    }

    /**
     * Desactiva un usuario (no lo elimina para conservar trazabilidad).
     */
    @Transactional
    public void desactivarUsuario(Long id) {
        Usuario u = obtenerPorId(id);
        u.setActivo(false);
        usuarioRepository.save(u);
    }

    /**
     * Cambia la contraseña de un usuario. Cifra con BCrypt (RNF04).
     */
    @Transactional
    public void cambiarPassword(Long id, String nuevaPassword) {
        Usuario u = obtenerPorId(id);
        u.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(u);
    }

    /**
     * Lista los cinco roles disponibles.
     */
    @Transactional(readOnly = true)
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }
}