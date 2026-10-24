package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio OrdenCompra (Tabla 48, fila 6). Soporta RF06, R-INV-05.
 */
@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    Optional<OrdenCompra> findByCodigo(String codigo);

    List<OrdenCompra> findByEstado(String estado);
}
