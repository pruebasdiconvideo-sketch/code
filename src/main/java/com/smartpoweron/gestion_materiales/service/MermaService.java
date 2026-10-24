package com.smartpoweron.gestion_materiales.service;

import com.smartpoweron.gestion_materiales.exception.ReglaNegocioException;
import com.smartpoweron.gestion_materiales.model.*;
import com.smartpoweron.gestion_materiales.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de mermas, devoluciones y trazabilidad
 * (RF24–RF28, P5-RN01, P5-RN02, R-INV-06, R-INV-07).
 * <p>
 * Implementa el Proceso 5 (Trazabilidad y Gestión de Mermas / Devoluciones)
 * del BPMN modelado en la sección 5.5 del informe.
 *
 * @author Grupo 8 - UTP
 */
@Service
@RequiredArgsConstructor
public class MermaService {

    private final MermaDevolucionRepository mermaRepository;
    private final MaterialRepository materialRepository;
    private final ProyectoRepository proyectoRepository;
    private final TrazabilidadRepository trazabilidadRepository;

    /**
     * RF24 - Registra una devolución de insumos sobrantes.
     */
    @Transactional
    public MermaDevolucion registrarDevolucion(Long idProyecto, Long idMaterial, Integer cantidad,
                                                 String estadoFisico, String motivo, Usuario operario) {
        if (cantidad == null || cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero", "MERMA-001");
        }
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ReglaNegocioException("Proyecto no encontrado", "PROY-404"));
        Material material = materialRepository.findById(idMaterial)
                .orElseThrow(() -> new ReglaNegocioException("Material no encontrado", "MAT-404"));

        MermaDevolucion md = MermaDevolucion.builder()
                .proyecto(proyecto)
                .material(material)
                .usuarioRegistra(operario)
                .tipo("DEVOLUCION")
                .estado("PENDIENTE")
                .cantidad(cantidad)
                .estadoFisico(estadoFisico)
                .motivo(motivo)
                .build();
        return mermaRepository.save(md);
    }

    /**
     * RF25, P5-RN01 - Reintegra materiales devueltos en buen estado al stock.
     * <p>
     * Verifica físicamente el almacenero. Si está en buen estado, el sistema
     * reintegra las unidades al stock y registra un movimiento DEVOLUCION
     * en Trazabilidad (R-INV-06).
     */
    @Transactional
    public MermaDevolucion reintegrarStock(Long idMerma, Usuario almacenero) {
        MermaDevolucion md = mermaRepository.findById(idMerma)
                .orElseThrow(() -> new ReglaNegocioException("Devolución no encontrada", "MERMA-404"));
        if (!"DEVOLUCION".equals(md.getTipo())) {
            throw new ReglaNegocioException("Solo se pueden reintegrar devoluciones (no mermas)", "MERMA-002");
        }
        if (!"PENDIENTE".equals(md.getEstado())) {
            throw new ReglaNegocioException("La devolución ya fue procesada", "MERMA-003");
        }

        // P5-RN01: reintegrar en menos de 24h
        Material m = md.getMaterial();
        int nuevoStock = m.getStockActual() + md.getCantidad();
        m.setStockActual(nuevoStock);
        materialRepository.save(m);

        // Registrar movimiento DEVOLUCION en Trazabilidad (R-INV-06)
        Trazabilidad t = Trazabilidad.builder()
                .material(m)
                .usuario(almacenero)
                .proyecto(md.getProyecto())
                .tipoMovimiento("DEVOLUCION")
                .cantidad(md.getCantidad())
                .saldoResultante(nuevoStock)
                .descripcion("Reintegro de devolución #" + md.getIdMerma())
                .build();
        trazabilidadRepository.save(t);

        md.setUsuarioValidador(almacenero);
        md.setEstado("REINTEGRADO");
        md.setFechaValidacion(LocalDateTime.now());
        return mermaRepository.save(md);
    }

    /**
     * RF26, P5-RN02 - Registra baja por merma de un componente defectuoso.
     * <p>
     * La baja requiere la validación del Jefe de Producción (P5-RN02).
     * Mientras no esté validada, no produce efecto alguno sobre el inventario.
     */
    @Transactional
    public MermaDevolucion registrarMerma(Long idProyecto, Long idMaterial, Integer cantidad,
                                            String motivo, Usuario operario) {
        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new ReglaNegocioException("Proyecto no encontrado", "PROY-404"));
        Material material = materialRepository.findById(idMaterial)
                .orElseThrow(() -> new ReglaNegocioException("Material no encontrado", "MAT-404"));

        MermaDevolucion md = MermaDevolucion.builder()
                .proyecto(proyecto)
                .material(material)
                .usuarioRegistra(operario)
                .tipo("MERMA")
                .estado("PENDIENTE")
                .cantidad(cantidad)
                .motivo(motivo)
                .build();
        return mermaRepository.save(md);
    }

    /**
     * RF26, P5-RN02 - Valida la baja por merma (solo JEFE_PRODUCCION).
     */
    @Transactional
    public MermaDevolucion validarBajaMerma(Long idMerma, Usuario jefeProduccion) {
        MermaDevolucion md = mermaRepository.findById(idMerma)
                .orElseThrow(() -> new ReglaNegocioException("Merma no encontrada", "MERMA-404"));
        if (!"MERMA".equals(md.getTipo())) {
            throw new ReglaNegocioException("Esta operación es para validar mermas, no devoluciones", "MERMA-004");
        }
        if (!"PENDIENTE".equals(md.getEstado())) {
            throw new ReglaNegocioException("La merma ya fue procesada", "MERMA-005");
        }
        md.setUsuarioValidador(jefeProduccion);
        md.setEstado("MERMADO");
        md.setFechaValidacion(LocalDateTime.now());
        mermaRepository.save(md);

        // Registrar movimiento MERMA en Trazabilidad (R-INV-06)
        // Nota: la merma no aumenta el stock; es una baja definitiva
        Trazabilidad t = Trazabilidad.builder()
                .material(md.getMaterial())
                .usuario(jefeProduccion)
                .proyecto(md.getProyecto())
                .tipoMovimiento("MERMA")
                .cantidad(md.getCantidad())
                .saldoResultante(md.getMaterial().getStockActual())
                .descripcion("Baja por merma validada #" + md.getIdMerma() + " - " + md.getMotivo())
                .build();
        trazabilidadRepository.save(t);
        return md;
    }

    /**
     * RF27 - Reporte consolidado de mermas por proyecto y periodo.
     */
    @Transactional(readOnly = true)
    public List<MermaDevolucion> reporteMermas(Long idProyecto, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime fin = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;
        return mermaRepository.reporteMermas(idProyecto, inicio, fin);
    }

    /**
     * RF28 - Consulta de trazabilidad de un material desde su ingreso hasta
     * su montaje o devolución final.
     */
    @Transactional(readOnly = true)
    public List<Trazabilidad> trazabilidadMaterial(String codigoMaterial) {
        Material material = materialRepository.findByCodigo(codigoMaterial)
                .orElseThrow(() -> new ReglaNegocioException("Material no encontrado", "MAT-404"));
        return trazabilidadRepository.findByMaterialIdMaterial(material.getIdMaterial());
    }

    @Transactional(readOnly = true)
    public List<MermaDevolucion> listarMermasPendientes() {
        return mermaRepository.findByEstado("PENDIENTE");
    }

    @Transactional(readOnly = true)
    public List<MermaDevolucion> listarTodasMermas() {
        return mermaRepository.findAll();
    }
}
