package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Rol (Tabla 48, fila 1).
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}
