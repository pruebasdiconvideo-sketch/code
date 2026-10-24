package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.dto.ApiResponse;
import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.repository.UsuarioRepository;
import com.smartpoweron.gestion_materiales.service.MermaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * API REST del módulo mermas, devoluciones y trazabilidad (RF24–RF28).
 * <p>
 * Endpoints:
 * <ul>
 *   <li>POST /api/merma/devoluciones (RF24) - registrar devolución</li>
 *   <li>POST /api/merma/devoluciones/{id}/reintegrar (RF25) - reintegrar stock</li>
 *   <li>POST /api/merma/mermas (RF26) - registrar merma</li>
 *   <li>POST /api/merma/mermas/{id}/validar (RF26, P5-RN02) - validar baja</li>
 *   <li>GET /api/merma/reportes (RF27) - reporte consolidado</li>
 *   <li>GET /api/merma/trazabilidad/{codigo} (RF28) - trazabilidad material</li>
 * </ul>
 *
 * @author Grupo 8 - UTP
 */
@RestController
@RequestMapping("/api/merma")
@RequiredArgsConstructor
public class MermaApiController {

    private final MermaService mermaService;
    private final UsuarioRepository usuarioRepository;

    /**
     * RF24 - Registra la devolución de insumos sobrantes.
     */
    @PostMapping("/devoluciones")
    @PreAuthorize("hasAnyRole('OPERARIO_ENSAMBLE', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> registrarDevolucion(
            @RequestParam Long idProyecto,
            @RequestParam Long idMaterial,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) String estadoFisico,
            @RequestParam(required = false) String motivo,
            Authentication auth) {
        Usuario operario = obtenerUsuario(auth);
        return ResponseEntity.ok(ApiResponse.ok("Devolución registrada",
                mermaService.registrarDevolucion(idProyecto, idMaterial, cantidad,
                        estadoFisico, motivo, operario)));
    }

    /**
     * RF25 - Reintegra materiales devueltos en buen estado al stock (P5-RN01).
     */
    @PostMapping("/devoluciones/{id}/reintegrar")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> reintegrar(@PathVariable Long id, Authentication auth) {
        Usuario almacenero = obtenerUsuario(auth);
        return ResponseEntity.ok(ApiResponse.ok("Stock reintegrado",
                mermaService.reintegrarStock(id, almacenero)));
    }

    /**
     * RF26 - Registra baja por merma.
     */
    @PostMapping("/mermas")
    @PreAuthorize("hasAnyRole('OPERARIO_ENSAMBLE', 'INSPECTOR_CALIDAD', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> registrarMerma(
            @RequestParam Long idProyecto,
            @RequestParam Long idMaterial,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) String motivo,
            Authentication auth) {
        Usuario usuario = obtenerUsuario(auth);
        return ResponseEntity.ok(ApiResponse.ok("Merma registrada",
                mermaService.registrarMerma(idProyecto, idMaterial, cantidad, motivo, usuario)));
    }

    /**
     * RF26, P5-RN02 - Valida la baja por merma (solo JEFE_PRODUCCION).
     */
    @PostMapping("/mermas/{id}/validar")
    @PreAuthorize("hasAnyRole('JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> validarMerma(@PathVariable Long id, Authentication auth) {
        Usuario jefe = obtenerUsuario(auth);
        return ResponseEntity.ok(ApiResponse.ok("Baja por merma validada",
                mermaService.validarBajaMerma(id, jefe)));
    }

    /**
     * RF27 - Reporte consolidado de mermas.
     */
    @GetMapping("/reportes")
    @PreAuthorize("hasAnyRole('JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> reporte(
            @RequestParam(required = false) Long idProyecto,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin) {
        return ResponseEntity.ok(ApiResponse.ok("Reporte de mermas",
                mermaService.reporteMermas(idProyecto, fechaInicio, fechaFin)));
    }

    /**
     * RF28 - Consulta de trazabilidad de un material.
     */
    @GetMapping("/trazabilidad/{codigo}")
    @PreAuthorize("hasAnyRole('JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> trazabilidad(@PathVariable String codigo) {
        return ResponseEntity.ok(ApiResponse.ok("Trazabilidad del material",
                mermaService.trazabilidadMaterial(codigo)));
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> pendientes() {
        return ResponseEntity.ok(ApiResponse.ok("Mermas/devoluciones pendientes",
                mermaService.listarMermasPendientes()));
    }

    private Usuario obtenerUsuario(Authentication auth) {
        String correo = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + correo));
    }
}
