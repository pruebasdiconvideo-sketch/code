package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Material (Tabla 48, fila 7). Soporta RF05–RF09.
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    Optional<Material> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Material> findByCategoria(String categoria);

    /** RF07: materiales con stock_actual ≤ stock_minimo (alertas). */
    @Query("SELECT m FROM Material m WHERE m.stockActual <= m.stockMinimo AND m.estado = 'ACTIVO'")
    List<Material> findMaterialesConStockBajo();

    /** RF08: búsqueda por código, nombre o categoría con LIKE (case insensitive). */
    @Query("SELECT m FROM Material m WHERE " +
           "(:codigo IS NULL OR LOWER(m.codigo) LIKE LOWER(CONCAT('%', :codigo, '%'))) AND " +
           "(:nombre IS NULL OR LOWER(m.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:categoria IS NULL OR LOWER(m.categoria) LIKE LOWER(CONCAT('%', :categoria, '%')))")
    List<Material> buscarPorFiltros(@Param("codigo") String codigo,
                                    @Param("nombre") String nombre,
                                    @Param("categoria") String categoria);

    List<Material> findByEstado(String estado);
}
