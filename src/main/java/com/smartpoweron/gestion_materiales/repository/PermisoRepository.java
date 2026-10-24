package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Permiso (Tabla 48, fila 2).
 */
@Repository
public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByCodigo(String codigo);
}
