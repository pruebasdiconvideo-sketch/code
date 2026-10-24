package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.DetalleSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio DetalleSolicitud (Tabla 48, fila 9). Soporta RF11, RF12, R-INV-02.
 */
@Repository
public interface DetalleSolicitudRepository extends JpaRepository<DetalleSolicitud, Long> {

    List<DetalleSolicitud> findBySolicitudIdSolicitud(Long idSolicitud);

    List<DetalleSolicitud> findByMaterialIdMaterial(Long idMaterial);
}
