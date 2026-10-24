package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.Trazabilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Trazabilidad (Tabla 48, fila 16). Soporta RF09, RF28, R-INV-07.
 */
@Repository
public interface TrazabilidadRepository extends JpaRepository<Trazabilidad, Long> {

    List<Trazabilidad> findByMaterialIdMaterial(Long idMaterial);

    List<Trazabilidad> findByProyectoIdProyecto(Long idProyecto);

    List<Trazabilidad> findByTipoMovimiento(String tipoMovimiento);

    List<Trazabilidad> findByUsuarioIdUsuario(Long idUsuario);
}
