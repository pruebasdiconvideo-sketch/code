package com.smartpoweron.gestion_materiales.service;

import com.smartpoweron.gestion_materiales.model.Material;
import com.smartpoweron.gestion_materiales.repository.MaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    // Listar todo el inventario (Consultable por todos los roles desde el home)
    public List<Material> listarMateriales() {
        return materialRepository.findAll();
    }

    // Lógica inteligente de Ingreso / Actualización (Proceso 1 - Almacén)
    public Material registrarOActualizarIngreso(Material materialNuevo, Integer cantidadIngresada) {
        // Buscamos si el material ya se encuentra registrado mediante su código único
        Optional<Material> materialExistente = materialRepository.findByCodigo(materialNuevo.getCodigo());

        if (materialExistente.isPresent()) {
            // CASO 1: EL MATERIAL YA EXISTE -> Actualizamos sumando al stock y refrescando ubicación
            Material materialAActualizar = materialExistente.get();
            materialAActualizar.setStockActual(materialAActualizar.getStockActual() + cantidadIngresada);
            
            // Actualizamos ubicación física por si el lote se movió de anaquel
            materialAActualizar.setPasillo(materialNuevo.getPasillo());
            materialAActualizar.setEstante(materialNuevo.getEstante());
            materialAActualizar.setNivel(materialNuevo.getNivel());

            return materialRepository.save(materialAActualizar);
        } else {
            // CASO 2: EL MATERIAL ES NUEVO -> Lo damos de alta con la cantidad inicial proporcionada
            materialNuevo.setStockActual(cantidadIngresada != null ? cantidadIngresada : 0);
            return materialRepository.save(materialNuevo);
        }
    }

    // Buscar material por ID para consultas específicas o edición
    public Material obtenerMaterialPorId(Long id) {
        return materialRepository.findById(id).orElse(null);
    }
}