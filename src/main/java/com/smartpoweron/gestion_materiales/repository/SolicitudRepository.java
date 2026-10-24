package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Solicitud (Tabla 48, fila 8). Soporta RF11–RF15.
 */
@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByEstado(String estado);

    List<Solicitud> findByProyectoIdProyecto(Long idProyecto);

    List<Solicitud> findByUsuarioSolicitanteIdUsuario(Long idUsuario);
}
