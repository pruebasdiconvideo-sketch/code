package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Proyecto (Tabla 48, fila 5). Soporta RF10, RF11.
 */
@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {

    Optional<Proyecto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Proyecto> findByEstado(String estado);
}
