package com.smartpoweron.gestion_materiales.service;

import com.smartpoweron.gestion_materiales.exception.ReglaNegocioException;
import com.smartpoweron.gestion_materiales.model.*;
import com.smartpoweron.gestion_materiales.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de despacho y entregas (RF15–RF19, P3-RN01, P3-RN02,
 * R-INV-01, R-INV-02, R-INV-03, R-INV-04).
 * <p>
 * Implementa el Proceso 3 (Despacho y Entrega de Kits en Almacén)
 * del BPMN modelado en la sección 5.3 del informe.
 *
 * @author Grupo 8 - UTP
 */
@Service
@RequiredArgsConstructor
public class DespachoService {

    private final DespachoRepository despachoRepository;
    private final DetalleDespachoRepository detalleDespachoRepository;
    private final SolicitudRepository solicitudRepository;
    private final MaterialRepository materialRepository;
    private final EntregaRepository entregaRepository;
    private final TrazabilidadRepository trazabilidadRepository;

    /**
     * RF16 - Valida que el código digitado coincida con el detalle de la solicitud
     * (P3-RN01: prohibida la salida de materiales sin validación digital).
     */
    @Transactional(readOnly = true)
    public boolean validarCodigoDespacho(Long idSolicitud, String codigoDigitado) {
        Solicitud s = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ReglaNegocioException("Solicitud no encontrada", "SOL-404"));
        // Busca si el código digitado corresponde a algún material de la BOM
        for (DetalleSolicitud det : s.getDetalles()) {
            if (det.getMaterial().getCodigo().equalsIgnoreCase(codigoDigitado)) {
                return true;
            }
        }
        return false;
    }

    /**
     * RF15, RF16, RF18 - Registra un despacho (COMPLETO o PARCIAL).
     * <p>
     * Crea el despacho con sus DetalleDespacho, validando cada código.
     * El stock NO se modifica aquí; se descuenta al confirmar la recepción.
     */
    @Transactional
    public Despacho registrarDespacho(Long idSolicitud, Usuario almacenero,
                                        List<DetalleDespacho> items, boolean esParcial,
                                        String observacion) {
        Solicitud solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ReglaNegocioException("Solicitud no encontrada", "SOL-404"));
        if (!"LISTA_DESPACHO".equals(solicitud.getEstado())) {
            throw new ReglaNegocioException(
                    "La solicitud no está lista para despacho (estado: " + solicitud.getEstado() + ")",
                    "P3-RN01");
        }

        Despacho despacho = Despacho.builder()
                .solicitud(solicitud)
                .usuarioAlmacenero(almacenero)
                .tipo(esParcial ? "PARCIAL" : "COMPLETO")
                .estado("PREPARANDO")
                .observacion(observacion)
                .detalles(new ArrayList<>())
                .build();

        // Validar cada código digitado (RF16, P3-RN01)
        for (DetalleDespacho item : items) {
            if (!validarCodigoDespacho(idSolicitud, item.getCodigoDigitado())) {
                throw new ReglaNegocioException(
                        "El código digitado '" + item.getCodigoDigitado()
                                + "' no coincide con el detalle de la solicitud.",
                        "P3-RN01");
            }
            // Verificar que la cantidad no exceda la pendiente
            DetalleSolicitud det = item.getDetalleSolicitud();
            int pendiente = det.getCantidadPendiente();
            if (item.getCantidadEntregada() > pendiente) {
                throw new ReglaNegocioException(
                        "La cantidad entregada (" + item.getCantidadEntregada()
                                + ") excede la pendiente (" + pendiente + ") para "
                                + det.getMaterial().getCodigo(),
                        "P3-RN02");
            }
            item.setDespacho(despacho);
            despacho.getDetalles().add(item);
        }

        return despachoRepository.save(despacho);
    }

    /**
     * RF17, RF19 - Confirma la recepción conforme del operario.
     * <p>
     * Transaccional: descuenta stock, acumula cantidad_despachada en
     * DetalleSolicitud, registra movimientos SALIDA en Trazabilidad
     * (R-INV-01). Si el despacho ya fue confirmado, se rechaza (R-INV-03).
     */
    @Transactional
    public Entrega confirmarEntrega(Long idDespacho, Usuario operario, String observacion) {
        Despacho despacho = despachoRepository.findById(idDespacho)
                .orElseThrow(() -> new ReglaNegocioException("Despacho no encontrado", "DESP-404"));

        // R-INV-03: confirmación sin duplicados
        if (entregaRepository.existsByDespachoIdDespachoAndConfirmadoTrue(idDespacho)) {
            throw new ReglaNegocioException(
                    "Este despacho ya fue confirmado. No se puede confirmar dos veces.",
                    "R-INV-03");
        }

        // R-INV-04: re-validar disponibilidad de stock
        for (DetalleDespacho det : despacho.getDetalles()) {
            Material m = det.getMaterial();
            if (m.getStockActual() < det.getCantidadEntregada()) {
                throw new ReglaNegocioException(
                        "Stock insuficiente para " + m.getCodigo()
                                + " (stock=" + m.getStockActual()
                                + ", requerido=" + det.getCantidadEntregada() + ")",
                        "R-INV-04");
            }
        }

        // R-INV-01: descuento de stock + acumulación en DetalleSolicitud + trazabilidad
        for (DetalleDespacho det : despacho.getDetalles()) {
            Material m = det.getMaterial();
            int nuevoStock = m.getStockActual() - det.getCantidadEntregada();
            m.setStockActual(nuevoStock);
            materialRepository.save(m);

            // Acumular cantidad_despachada en DetalleSolicitud
            DetalleSolicitud ds = det.getDetalleSolicitud();
            ds.setCantidadDespachada(ds.getCantidadDespachada() + det.getCantidadEntregada());

            // Registrar movimiento SALIDA en Trazabilidad
            Trazabilidad t = Trazabilidad.builder()
                    .material(m)
                    .usuario(operario)
                    .proyecto(despacho.getSolicitud().getProyecto())
                    .tipoMovimiento("SALIDA")
                    .cantidad(det.getCantidadEntregada())
                    .saldoResultante(nuevoStock)
                    .descripcion("Salida por despacho #" + despacho.getIdDespacho()
                            + " a proyecto " + despacho.getSolicitud().getProyecto().getCodigo())
                    .idDespachoOrigen(despacho.getIdDespacho())
                    .build();
            trazabilidadRepository.save(t);
        }

        // Verificar si la solicitud quedó totalmente despachada
        Solicitud solicitud = despacho.getSolicitud();
        boolean totalmenteDespachada = solicitud.getDetalles().stream()
                .allMatch(DetalleSolicitud::isCompletamenteDespachada);
        if (totalmenteDespachada) {
            solicitud.setEstado("DESPACHADA");
            solicitudRepository.save(solicitud);
        }
        despacho.setEstado("CONFIRMADO");
        despachoRepository.save(despacho);

        // Crear la Entrega
        Entrega entrega = Entrega.builder()
                .despacho(despacho)
                .usuarioOperario(operario)
                .confirmado(true)
                .fechaEntrega(LocalDateTime.now())
                .observacion(observacion)
                .build();
        return entregaRepository.save(entrega);
    }

    /**
     * Lista despachos de un almacenero.
     */
    @Transactional(readOnly = true)
    public List<Despacho> listarDespachosPorAlmacenero(Long idUsuario) {
        return despachoRepository.findByUsuarioAlmaceneroIdUsuario(idUsuario);
    }

    /**
     * Lista despachos de una solicitud (para conocer el avance).
     */
    @Transactional(readOnly = true)
    public List<Despacho> listarDespachosPorSolicitud(Long idSolicitud) {
        return despachoRepository.findBySolicitudIdSolicitud(idSolicitud);
    }

    @Transactional(readOnly = true)
    public Optional<Despacho> obtenerDespacho(Long id) {
        return despachoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Despacho> listarTodosDespachos() {
        return despachoRepository.findAll();
    }
}
