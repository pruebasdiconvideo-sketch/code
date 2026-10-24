package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.MermaDevolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio MermaDevolucion (Tabla 48, fila 15). Soporta RF24–RF27.
 */
@Repository
public interface MermaDevolucionRepository extends JpaRepository<MermaDevolucion, Long> {

    List<MermaDevolucion> findByProyectoIdProyecto(Long idProyecto);

    List<MermaDevolucion> findByEstado(String estado);

    List<MermaDevolucion> findByTipo(String tipo);

    /** RF27: reporte de mermas por proyecto y rango de fechas. */
    @Query("SELECT m FROM MermaDevolucion m WHERE " +
           "(:idProyecto IS NULL OR m.proyecto.idProyecto = :idProyecto) AND " +
           "m.fechaRegistro BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY m.fechaRegistro DESC")
    List<MermaDevolucion> reporteMermas(@Param("idProyecto") Long idProyecto,
                                        @Param("fechaInicio") LocalDateTime fechaInicio,
                                        @Param("fechaFin") LocalDateTime fechaFin);
}
