package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Despacho (Tabla 48, fila 10). Soporta RF15, RF18.
 */
@Repository
public interface DespachoRepository extends JpaRepository<Despacho, Long> {

    List<Despacho> findBySolicitudIdSolicitud(Long idSolicitud);

    List<Despacho> findByUsuarioAlmaceneroIdUsuario(Long idUsuario);

    List<Despacho> findByEstado(String estado);
}
