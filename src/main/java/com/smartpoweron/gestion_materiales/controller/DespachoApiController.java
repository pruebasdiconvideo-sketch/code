package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.dto.ApiResponse;
import com.smartpoweron.gestion_materiales.model.DetalleDespacho;
import com.smartpoweron.gestion_materiales.model.Despacho;
import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.repository.UsuarioRepository;
import com.smartpoweron.gestion_materiales.service.DespachoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST del módulo despacho (RF15–RF19, P3-RN01, P3-RN02).
 * <p>
 * Endpoints:
 * <ul>
 *   <li>GET /api/despacho/validar (RF16) - validar código digitado</li>
 *   <li>POST /api/despacho (RF15, RF18) - registrar despacho</li>
 *   <li>POST /api/despacho/{id}/confirmar (RF17, RF19) - confirmar recepción</li>
 *   <li>GET /api/despacho - listar todos los despachos</li>
 * </ul>
 *
 * @author Grupo 8 - UTP
 */
@RestController
@RequestMapping("/api/despacho")
@RequiredArgsConstructor
public class DespachoApiController {

    private final DespachoService despachoService;
    private final UsuarioRepository usuarioRepository;

    /**
     * RF16 - Valida que el código digitado coincida con el detalle de la solicitud.
     * P3-RN01: prohibida la salida de materiales sin validación digital.
     */
    @GetMapping("/validar")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Boolean>> validarCodigo(
            @RequestParam Long idSolicitud,
            @RequestParam String codigo) {
        return ResponseEntity.ok(ApiResponse.ok("Validación de código",
                despachoService.validarCodigoDespacho(idSolicitud, codigo)));
    }

    /**
     * RF15, RF18 - Registra un despacho (completo o parcial).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ALMACENERO', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Despacho>> registrarDespacho(
            @RequestParam Long idSolicitud,
            @RequestParam(defaultValue = "false") boolean parcial,
            @RequestParam(required = false) String observacion,
            @RequestBody List<DetalleDespacho> items,
            Authentication auth) {
        Usuario almacenero = obtenerUsuario(auth);
        Despacho d = despachoService.registrarDespacho(idSolicitud, almacenero, items, parcial, observacion);
        return ResponseEntity.ok(ApiResponse.ok("Despacho registrado", d));
    }

    /**
     * RF17, RF19 - Confirma la recepción conforme del operario.
     * <p>
     * Transaccional: descuenta stock, acumula en DetalleSolicitud y registra
     * trazabilidad (R-INV-01). Rechaza duplicados (R-INV-03).
     */
    @PostMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('OPERARIO_ENSAMBLE', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> confirmarEntrega(
            @PathVariable Long id,
            @RequestParam(required = false) String observacion,
            Authentication auth) {
        Usuario operario = obtenerUsuario(auth);
        return ResponseEntity.ok(ApiResponse.ok("Recepción confirmada y stock descontado",
                despachoService.confirmarEntrega(id, operario, observacion)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ALMACENERO', 'OPERARIO_ENSAMBLE', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de despachos",
                despachoService.listarTodosDespachos()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'OPERARIO_ENSAMBLE', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Detalle del despacho",
                despachoService.obtenerDespacho(id)));
    }

    private Usuario obtenerUsuario(Authentication auth) {
        String correo = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + correo));
    }
}
