package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.PruebaCalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio PruebaCalidad (Tabla 48, fila 14). Soporta RF20–RF23, R-CAL-02.
 */
@Repository
public interface PruebaCalidadRepository extends JpaRepository<PruebaCalidad, Long> {

    List<PruebaCalidad> findByProyectoIdProyecto(Long idProyecto);

    List<PruebaCalidad> findByProyectoIdProyectoAndVigenteTrue(Long idProyecto);

    /** R-CAL-02: prueba vigente CONFORME o NO_CONFORME por proyecto+parámetro. */
    @Query("SELECT p FROM PruebaCalidad p WHERE p.proyecto.idProyecto = :idProyecto " +
           "AND p.parametro.idParametro = :idParametro AND p.vigente = true")
    PruebaCalidad findPruebaVigente(@Param("idProyecto") Long idProyecto,
                                    @Param("idParametro") Long idParametro);

    /** R-CAL-04: al insertar nueva, las anteriores quedan no vigentes. */
    @Query("SELECT p FROM PruebaCalidad p WHERE p.proyecto.idProyecto = :idProyecto " +
           "AND p.parametro.idParametro = :idParametro AND p.vigente = true")
    List<PruebaCalidad> findVigentesPorProyectoYParametro(@Param("idProyecto") Long idProyecto,
                                                          @Param("idParametro") Long idParametro);
}
