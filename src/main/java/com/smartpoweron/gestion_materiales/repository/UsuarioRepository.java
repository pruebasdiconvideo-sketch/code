package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Usuario (Tabla 48, fila 4). Soporta RF01, RF03.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Usuario> findByActivoTrue();

    List<Usuario> findByRolNombre(String rolNombre);

    @Modifying
    @Query("UPDATE Usuario u SET u.ultimoAcceso = :fecha WHERE u.idUsuario = :id")
    void actualizarUltimoAcceso(@Param("id") Long id, @Param("fecha") LocalDateTime fecha);
}
