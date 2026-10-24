package com.smartpoweron.gestion_materiales.service;

import com.smartpoweron.gestion_materiales.exception.ReglaNegocioException;
import com.smartpoweron.gestion_materiales.model.Material;
import com.smartpoweron.gestion_materiales.model.OrdenCompra;
import com.smartpoweron.gestion_materiales.model.Trazabilidad;
import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.repository.MaterialRepository;
import com.smartpoweron.gestion_materiales.repository.OrdenCompraRepository;
import com.smartpoweron.gestion_materiales.repository.TrazabilidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de inventario y catálogo (RF05–RF09, P1-RN01, P1-RN02, R-INV-05).
 * <p>
 * Implementa el Proceso 1 (Gestión de Inventario y Catálogo de Materiales)
 * del BPMN modelado en la sección 5.1 del informe.
 *
 * @author Grupo 8 - UTP
 */
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final TrazabilidadRepository trazabilidadRepository;

    /**
     * RF05 - Registra un nuevo material en el catálogo (P1-RN01).
     * <p>
     * Valida que el código de fabricante sea único y obligatorio, que el
     * nombre y la especificación técnica no estén vacíos.
     */
    @Transactional
    public Material registrarMaterial(Material material) {
        // Validación P1-RN01: campos obligatorios
        if (material.getCodigo() == null || material.getCodigo().isBlank()) {
            throw new ReglaNegocioException("El código de fabricante es obligatorio", "P1-RN01");
        }
        if (material.getNombre() == null || material.getNombre().isBlank()) {
            throw new ReglaNegocioException("El nombre del material es obligatorio", "P1-RN01");
        }
        if (material.getEspecificacionTecnica() == null || material.getEspecificacionTecnica().isBlank()) {
            throw new ReglaNegocioException("La especificación técnica es obligatoria", "P1-RN01");
        }
        if (material.getStockMinimo() == null || material.getStockMinimo() < 0) {
            throw new ReglaNegocioException("El stock mínimo es obligatorio y numérico", "P1-RN01");
        }
        // Validación de unicidad
        if (materialRepository.existsByCodigo(material.getCodigo())) {
            throw new ReglaNegocioException("El código ya se encuentra registrado", "MAT-001");
        }
        if (material.getStockActual() == null) material.setStockActual(0);
        if (material.getEstado() == null) material.setEstado("ACTIVO");
        return materialRepository.save(material);
    }

    /**
     * RF06 - Registra el ingreso de materiales validado contra la OC (P1-RN02, R-INV-05).
     * <p>
     * Solo aumenta el stock si la OC existe y está PENDIENTE o CONFORME.
     * Registra un movimiento ENTRADA en Trazabilidad con el id_orden_compra.
     */
    @Transactional
    public Material registrarIngresoValidado(String codigoMaterial, Integer cantidad, Long idOrdenCompra,
                                              Usuario usuario) {
        if (cantidad == null || cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero", "P1-RN02");
        }
        Material material = materialRepository.findByCodigo(codigoMaterial)
                .orElseThrow(() -> new ReglaNegocioException("Material no encontrado: " + codigoMaterial, "MAT-404"));
        OrdenCompra oc = ordenCompraRepository.findById(idOrdenCompra)
                .orElseThrow(() -> new ReglaNegocioException("Orden de compra no encontrada", "OC-404"));

        // P1-RN02: el stock solo aumenta si la OC es válida
        if ("CERRADA".equals(oc.getEstado()) || "ANULADA".equals(oc.getEstado())) {
            throw new ReglaNegocioException(
                    "La orden de compra está " + oc.getEstado() + ". No se puede registrar el ingreso.", "P1-RN02");
        }

        material.setStockActual(material.getStockActual() + cantidad);
        Material saved = materialRepository.save(material);

        // R-INV-05 / RF09: registro en Trazabilidad
        Trazabilidad t = Trazabilidad.builder()
                .material(saved)
                .usuario(usuario)
                .ordenCompra(oc)
                .tipoMovimiento("ENTRADA")
                .cantidad(cantidad)
                .saldoResultante(saved.getStockActual())
                .descripcion("Ingreso validado contra OC " + oc.getCodigo())
                .build();
        trazabilidadRepository.save(t);
        return saved;
    }

    /**
     * RF07 - Lista materiales con stock_actual ≤ stock_minimo (alerta visual).
     */
    @Transactional(readOnly = true)
    public List<Material> listarAlertasStockBajo() {
        return materialRepository.findMaterialesConStockBajo();
    }

    /**
     * RF08 - Consulta del catálogo con filtros por código, nombre o categoría.
     * Garantiza respuesta en máximo 3 segundos (RNF02).
     */
    @Transactional(readOnly = true)
    public List<Material> consultarCatalogo(String codigo, String nombre, String categoria) {
        return materialRepository.buscarPorFiltros(codigo, nombre, categoria);
    }

    /**
     * Lista todos los materiales activos.
     */
    @Transactional(readOnly = true)
    public List<Material> listarMateriales() {
        return materialRepository.findByEstado("ACTIVO");
    }

    /**
     * RF05 / RF06 - Búsqueda por ID.
     */
    @Transactional(readOnly = true)
    public Material obtenerPorId(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("Material no encontrado", "MAT-404"));
    }

    /**
     * Búsqueda por código (usado por el despacho para validar RF16).
     */
    @Transactional(readOnly = true)
    public Optional<Material> obtenerPorCodigo(String codigo) {
        return materialRepository.findByCodigo(codigo);
    }

    /**
     * RF09 - Historial de movimientos de un material (Trazabilidad).
     */
    @Transactional(readOnly = true)
    public List<Trazabilidad> historialMovimientos(Long idMaterial) {
        return trazabilidadRepository.findByMaterialIdMaterial(idMaterial);
    }
}
