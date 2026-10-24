package com.smartpoweron.gestion_materiales.service;

import com.smartpoweron.gestion_materiales.dto.ItemBomDto;
import com.smartpoweron.gestion_materiales.dto.SolicitudBomDto;
import com.smartpoweron.gestion_materiales.exception.ReglaNegocioException;
import com.smartpoweron.gestion_materiales.model.*;
import com.smartpoweron.gestion_materiales.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de proyectos y solicitudes (RF10–RF14, P2-RN01, P2-RN02).
 * <p>
 * Implementa el Proceso 2 (Solicitud y Validación de Materiales por Proyecto)
 * del BPMN modelado en la sección 5.2 del informe.
 *
 * @author Grupo 8 - UTP
 */
@Service
@RequiredArgsConstructor
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;
    private final SolicitudRepository solicitudRepository;
    private final MaterialRepository materialRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * RF10 - Crea un proyecto de armado de tablero.
     */
    @Transactional
    public Proyecto crearProyecto(Proyecto proyecto, Usuario jefeProduccion) {
        if (proyectoRepository.existsByCodigo(proyecto.getCodigo())) {
            throw new ReglaNegocioException("El código del proyecto ya existe", "PROY-001");
        }
        if (proyecto.getPresupuesto() == null || proyecto.getPresupuesto().signum() < 0) {
            throw new ReglaNegocioException("El presupuesto debe ser positivo", "PROY-002");
        }
        if (proyecto.getFechaInicio() == null) {
            throw new ReglaNegocioException("La fecha de inicio es obligatoria", "PROY-003");
        }
        proyecto.setEstado("ACTIVO");
        return proyectoRepository.save(proyecto);
    }

    /**
     * RF11, RF12, RF13, RF14 - Registra una solicitud de materiales con BOM.
     * <p>
     * P2-RN01: solo componentes del catálogo (validación automática).
     * P2-RN02: si costo_total > presupuesto → estado RETENIDA.
     * RF13: si stock suficiente → estado LISTA_DESPACHO.
     */
    @Transactional
    public Solicitud registrarSolicitudConBom(SolicitudBomDto dto, Usuario solicitante) {
        Proyecto proyecto = proyectoRepository.findById(dto.getIdProyecto())
                .orElseThrow(() -> new ReglaNegocioException("Proyecto no encontrado", "PROY-404"));

        Solicitud solicitud = Solicitud.builder()
                .proyecto(proyecto)
                .usuarioSolicitante(solicitante)
                .estado("BORRADOR")
                .detalles(new ArrayList<>())
                .build();

        BigDecimal costoTotal = BigDecimal.ZERO;
        boolean stockSuficiente = true;
        List<String> faltantes = new ArrayList<>();

        for (ItemBomDto item : dto.getItems()) {
            // P2-RN01 + RF12: validar que el material exista en el catálogo
            Material material = materialRepository.findByCodigo(item.getCodigoMaterial())
                    .orElseThrow(() -> new ReglaNegocioException(
                            "El material con código " + item.getCodigoMaterial() + " no existe en el catálogo",
                            "P2-RN01"));

            BigDecimal precio = item.getPrecioUnitario() != null
                    ? item.getPrecioUnitario()
                    : (material.getPrecioUnitario() != null ? material.getPrecioUnitario() : BigDecimal.ZERO);
            precio = precio.setScale(2, RoundingMode.HALF_UP);

            DetalleSolicitud det = DetalleSolicitud.builder()
                    .solicitud(solicitud)
                    .material(material)
                    .cantidadSolicitada(item.getCantidad())
                    .cantidadDespachada(0)
                    .precioUnitario(precio)
                    .build();
            solicitud.addDetalle(det);

            costoTotal = costoTotal.add(precio.multiply(BigDecimal.valueOf(item.getCantidad())));

            // RF13: verificar disponibilidad de stock
            if (material.getStockActual() < item.getCantidad()) {
                stockSuficiente = false;
                faltantes.add(material.getCodigo() + " (stock=" + material.getStockActual()
                        + ", solicitado=" + item.getCantidad() + ")");
            }
        }

        solicitud.setCostoTotal(costoTotal);

        // RF14 / P2-RN02: retención por presupuesto
        if (costoTotal.compareTo(proyecto.getPresupuesto()) > 0) {
            solicitud.setEstado("RETENIDA");
            solicitud.setMotivoRetencion("Costo total S/ " + String.format("%.2f", costoTotal)
                    + " excede el presupuesto S/ " + String.format("%.2f", proyecto.getPresupuesto()));
        } else if (stockSuficiente) {
            // RF13: si hay stock, pasa a LISTA_DESPACHO y se notifica al almacenero
            solicitud.setEstado("LISTA_DESPACHO");
        } else {
            // Stock insuficiente: queda en BORRADOR y se notifica a producción
            solicitud.setEstado("BORRADOR");
            solicitud.setMotivoRetencion("Stock insuficiente: " + String.join(", ", faltantes));
        }

        return solicitudRepository.save(solicitud);
    }

    /**
     * RF14 - Aprueba una solicitud retenida (solo ADMINISTRADOR).
     */
    @Transactional
    public Solicitud aprobarSolicitudRetenida(Long idSolicitud, Usuario aprobador) {
        Solicitud s = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ReglaNegocioException("Solicitud no encontrada", "SOL-404"));
        if (!"RETENIDA".equals(s.getEstado())) {
            throw new ReglaNegocioException(
                    "La solicitud no está retenida, no se puede aprobar", "SOL-002");
        }
        s.setUsuarioAprobador(aprobador);
        s.setEstado("LISTA_DESPACHO");
        s.setFechaAprobacion(LocalDateTime.now());
        return solicitudRepository.save(s);
    }

    /**
     * RF15 - Lista solicitudes LISTA_DESPACHO para la bandeja del almacenero.
     */
    @Transactional(readOnly = true)
    public List<Solicitud> listarPendientesDespacho() {
        return solicitudRepository.findByEstado("LISTA_DESPACHO");
    }

    /**
     * Lista todas las solicitudes.
     */
    @Transactional(readOnly = true)
    public List<Solicitud> listarSolicitudes() {
        return solicitudRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Proyecto> listarProyectos() {
        return proyectoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Proyecto obtenerProyecto(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("Proyecto no encontrado", "PROY-404"));
    }

    @Transactional(readOnly = true)
    public Solicitud obtenerSolicitud(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("Solicitud no encontrada", "SOL-404"));
    }
}
