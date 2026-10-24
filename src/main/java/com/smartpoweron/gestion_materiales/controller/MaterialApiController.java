package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.dto.ApiResponse;
import com.smartpoweron.gestion_materiales.model.Material;
import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.repository.UsuarioRepository;
import com.smartpoweron.gestion_materiales.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST del módulo inventario (RF05–RF09).
 * <p>
 * Endpoints:
 * <ul>
 *   <li>POST /api/almacen/materiales (RF05) - registrar material</li>
 *   <li>GET /api/almacen/materiales (RF08) - listar / filtrar catálogo</li>
 *   <li>POST /api/almacen/materiales/ingreso (RF06, P1-RN02) - ingreso validado contra OC</li>
 *   <li>GET /api/almacen/materiales/alertas (RF07) - alertas de stock bajo</li>
 *   <li>GET /api/almacen/materiales/{id}/historial (RF09) - historial de movimientos</li>
 * </ul>
 *
 * @author Grupo 8 - UTP
 */
@RestController
@RequestMapping("/api/almacen")
@RequiredArgsConstructor
public class MaterialApiController {

    private final MaterialService materialService;
    private final UsuarioRepository usuarioRepository;

    /**
     * RF05 - Registrar nuevo material en catálogo.
     */
    @PostMapping("/materiales")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Material>> registrar(@Valid @RequestBody Material material) {
        Material guardado = materialService.registrarMaterial(material);
        return ResponseEntity.ok(ApiResponse.ok("Material registrado correctamente", guardado));
    }

    /**
     * RF08 - Consultar catálogo con filtros.
     */
    @GetMapping("/materiales")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<Material>>> listar(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria) {
        return ResponseEntity.ok(ApiResponse.ok("Catálogo consultado",
                materialService.consultarCatalogo(codigo, nombre, categoria)));
    }

    /**
     * RF06 - Ingreso validado contra orden de compra (P1-RN02).
     */
    @PostMapping("/materiales/{codigo}/ingreso")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<Material>> ingreso(
            @PathVariable String codigo,
            @RequestParam Integer cantidad,
            @RequestParam Long idOrdenCompra,
            Authentication auth) {
        String correo = ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername();
        Usuario almacenero = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + correo));
        return ResponseEntity.ok(ApiResponse.ok("Ingreso registrado",
                materialService.registrarIngresoValidado(codigo, cantidad, idOrdenCompra, almacenero)));
    }

    /**
     * RF07 - Materiales con stock bajo.
     */
    @GetMapping("/materiales/alertas")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<List<Material>>> alertas() {
        return ResponseEntity.ok(ApiResponse.ok("Alertas de stock mínimo",
                materialService.listarAlertasStockBajo()));
    }

    /**
     * RF09 - Historial de movimientos de un material.
     */
    @GetMapping("/materiales/{codigo}/historial")
    @PreAuthorize("hasAnyRole('ALMACENERO', 'JEFE_PRODUCCION', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponse<?>> historial(@PathVariable String codigo) {
        var material = materialService.obtenerPorCodigo(codigo);
        if (material.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("Material no encontrado", "MAT-404"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Historial de movimientos",
                materialService.historialMovimientos(material.get().getIdMaterial())));
    }
}
