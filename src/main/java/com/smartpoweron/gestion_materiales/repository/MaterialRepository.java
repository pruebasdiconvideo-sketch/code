package com.smartpoweron.gestion_materiales.repository;

import com.smartpoweron.gestion_materiales.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    // Buscar material por su código único de fabricante o sistema
    Optional<Material> findByCodigo(String codigo);

    // Filtrar materiales por categoría (Equipos vs Insumos)
    List<Material> findByCategoria(String categoria);

    // Encontrar materiales cuyo stock actual sea menor o igual al stock mínimo (Para alertas automáticas)
    List<Material> findByStockActualLessThanEqual(Integer stockMinimo);
}