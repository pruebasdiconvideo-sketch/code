package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.DetalleDespacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio DetalleDespacho (Tabla 48, fila 11). Soporta RF16, P3-RN01.
 */
@Repository
public interface DetalleDespachoRepository extends JpaRepository<DetalleDespacho, Long> {

    List<DetalleDespacho> findByDespachoIdDespacho(Long idDespacho);

    List<DetalleDespacho> findByMaterialIdMaterial(Long idMaterial);
}
