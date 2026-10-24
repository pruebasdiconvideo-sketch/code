package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.ParametroPrueba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio ParametroPrueba (Tabla 48, fila 13). Soporta RF21, R-CAL-01.
 */
@Repository
public interface ParametroPruebaRepository extends JpaRepository<ParametroPrueba, Long> {

    Optional<ParametroPrueba> findByTipoPrueba(String tipoPrueba);

    List<ParametroPrueba> findByActivoTrue();
}
