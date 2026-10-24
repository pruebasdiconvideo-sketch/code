package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.Entrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio Entrega (Tabla 48, fila 12). Soporta RF17, RF19, R-INV-03.
 */
@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    Optional<Entrega> findByDespachoIdDespacho(Long idDespacho);

    boolean existsByDespachoIdDespachoAndConfirmadoTrue(Long idDespacho);

    Optional<Entrega> findByDespachoIdDespachoAndConfirmadoTrue(Long idDespacho);
}
