package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.dto.ApiResponse;
import com.smartpoweron.gestion_materiales.dto.SolicitudBomDto;
import com.smartpoweron.gestion_materiales.model.Proyecto;
import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.repository.UsuarioRepository;
import com.smartpoweron.gestion_materiales.service.ProyectoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * API REST del módulo proyectos y solicitudes (RF10–RF14, P2-RN02).
 * <p>
 * Endpoints:
 * <ul>
 *   <li>POST /api/produccion/proyectos (RF10) - crear proyecto</li>
 *   <li>GET /api/produccion/proyectos - listar proyectos</li>
 *   <li>POST /api/produccion/solicitudes (RF11-RF14) - registrar solicitud con BOM</li>
 *   <li>POST /api/produccion/solicitudes/{id}/aprobar (RF14) - aprobar retenida</li>
 *   <li>GET /api/produccion/solicitudes/pendientes (RF15) - bandeja despacho</li>
 * </ul>
 *
 * @author Grupo 8 - UTP
 */
@RestController
@RequestMapping("/api/produccion")
@RequiredArgsConstructor
public class ProyectoApiController {

    private final ProyectoService proyectoService;
    private final UsuarioRepository usuarioRepository;

    /**
     * RF10 - Crear un proyecto.
     */
    @PostMapping("/proyectos")
    @PreAuthorize("hasAnyRole('JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Proyecto>> crearProyecto(@Valid @RequestBody Proyecto proyecto,
                                                                Authentication auth) {
        Usuario jefe = obtenerUsuario(auth);
        return ResponseEntity.ok(ApiResponse.ok("Proyecto creado",
                proyectoService.crearProyecto(proyecto, jefe)));
    }

    /**
     * Listar todos los proyectos.
     */
    @GetMapping("/proyectos")
    @PreAuthorize("hasAnyRole('JEFE_PRODUCCION', 'ADMINISTRADOR', 'INSPECTOR_CALIDAD')")
    public ResponseEntity<ApiResponse<?>> listarProyectos() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de proyectos",
                proyectoService.listarProyectos()));
    }

    /**
     * RF11, RF12, RF13, RF14 - Registrar solicitud con BOM.
     */
    @PostMapping("/solicitudes")
    @PreAuthorize("hasAnyRole('JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> registrarSolicitud(@Valid @RequestBody SolicitudBomDto dto,
                                                              Authentication auth) {
        Usuario solicitante = obtenerUsuario(auth);
        return ResponseEntity.ok(ApiResponse.ok("Solicitud registrada",
                proyectoService.registrarSolicitudConBom(dto, solicitante)));
    }

    /**
     * RF14 - Aprobar solicitud retenida (solo ADMINISTRADOR).
     */
    @PostMapping("/solicitudes/{id}/aprobar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> aprobarRetenida(@PathVariable Long id, Authentication auth) {
        Usuario aprobador = obtenerUsuario(auth);
        return ResponseEntity.ok(ApiResponse.ok("Solicitud aprobada",
                proyectoService.aprobarSolicitudRetenida(id, aprobador)));
    }

    /**
     * RF15 - Bandeja de solicitudes pendientes de despacho.
     */
    @GetMapping("/solicitudes/pendientes")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> pendientesDespacho() {
        return ResponseEntity.ok(ApiResponse.ok("Bandeja de despachos pendientes",
                proyectoService.listarPendientesDespacho()));
    }

    /**
     * Helper para obtener el Usuario completo desde la autenticación.
     */
    private Usuario obtenerUsuario(Authentication auth) {
        String correo = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + correo));
    }
}
