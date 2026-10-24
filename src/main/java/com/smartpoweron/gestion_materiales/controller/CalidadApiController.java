package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.dto.ApiResponse;
import com.smartpoweron.gestion_materiales.model.ParametroPrueba;
import com.smartpoweron.gestion_materiales.model.PruebaCalidad;
import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.repository.UsuarioRepository;
import com.smartpoweron.gestion_materiales.service.CalidadService;
import com.smartpoweron.gestion_materiales.service.PdfCertificadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * API REST del módulo de calidad (RF20–RF23).
 * <p>
 * Endpoints:
 * <ul>
 *   <li>POST /api/calidad/pruebas (RF20, RF21) - registrar medición</li>
 *   <li>GET /api/calidad/proyectos/{id}/pruebas (RF20) - historial</li>
 *   <li>GET /api/calidad/proyectos/{id}/puede-certificar (RF22)</li>
 *   <li>GET /api/calidad/proyectos/{id}/certificado.pdf (RF22) - descargar PDF</li>
 *   <li>GET /api/calidad/proyectos/{id}/observaciones (RF23)</li>
 *   <li>POST /api/calidad/parametros (RF21) - crear parámetro</li>
 * </ul>
 *
 * @author Grupo 8 - UTP
 */
@RestController
@RequestMapping("/api/calidad")
@RequiredArgsConstructor
public class CalidadApiController {

    private final CalidadService calidadService;
    private final UsuarioRepository usuarioRepository;
    private final PdfCertificadoService pdfCertificadoService;

    /**
     * RF20, RF21 - Registra una medición. El resultado lo calcula el servicio.
     */
    @PostMapping("/pruebas")
    @PreAuthorize("hasAnyRole('INSPECTOR_CALIDAD', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<PruebaCalidad>> registrarPrueba(
            @RequestParam Long idProyecto,
            @RequestParam Long idParametro,
            @RequestParam BigDecimal valorMedido,
            @RequestParam(required = false) String observacion,
            Authentication auth) {
        Usuario inspector = obtenerUsuario(auth);
        return ResponseEntity.ok(ApiResponse.ok("Prueba registrada",
                calidadService.registrarPrueba(idProyecto, idParametro, valorMedido,
                        inspector, observacion)));
    }

    @GetMapping("/proyectos/{id}/pruebas")
    @PreAuthorize("hasAnyRole('INSPECTOR_CALIDAD', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> listarPruebas(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Pruebas del proyecto",
                calidadService.listarPruebasPorProyecto(id)));
    }

    @GetMapping("/proyectos/{id}/observaciones")
    @PreAuthorize("hasAnyRole('INSPECTOR_CALIDAD', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> observaciones(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Órdenes de observación",
                calidadService.listarOrdenesObservacion(id)));
    }

    @GetMapping("/proyectos/{id}/puede-certificar")
    @PreAuthorize("hasAnyRole('INSPECTOR_CALIDAD', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Boolean>> puedeCertificar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Estado de certificación",
                calidadService.puedeGenerarCertificado(id)));
    }

    /**
     * RF22 - Descarga el certificado de conformidad en PDF.
     * Genera 404 si el tablero no puede certificarse (R-CAL-02).
     */
    @GetMapping("/proyectos/{id}/certificado.pdf")
    @PreAuthorize("hasAnyRole('INSPECTOR_CALIDAD', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ByteArrayResource> descargarCertificado(@PathVariable Long id) {
        byte[] pdf = pdfCertificadoService.generarCertificado(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=certificado_proyecto_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(pdf));
    }

    @PostMapping("/parametros")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<ParametroPrueba>> crearParametro(
            @RequestBody ParametroPrueba parametro) {
        return ResponseEntity.ok(ApiResponse.ok("Parámetro creado",
                calidadService.crearParametro(parametro)));
    }

    @GetMapping("/parametros")
    @PreAuthorize("hasAnyRole('INSPECTOR_CALIDAD', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> listarParametros() {
        return ResponseEntity.ok(ApiResponse.ok("Parámetros de prueba",
                calidadService.listarParametros()));
    }

    private Usuario obtenerUsuario(Authentication auth) {
        String correo = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + correo));
    }
}
