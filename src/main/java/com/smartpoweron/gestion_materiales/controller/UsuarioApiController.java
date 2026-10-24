package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.dto.ApiResponse;
import com.smartpoweron.gestion_materiales.model.Usuario;
import com.smartpoweron.gestion_materiales.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * API REST para gestión de usuarios (RF03).
 * Solo ADMINISTRADOR puede crear/editar/desactivar.
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequiredArgsConstructor
public class UsuarioApiController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<ApiResponse<Usuario>> crear(@RequestBody Usuario usuario,
                                                      @RequestParam String passwordInicial) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario creado",
                usuarioService.crearUsuario(usuario, passwordInicial)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Listado de usuarios",
                usuarioService.listarUsuarios()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Usuario>> editar(@PathVariable Long id, @RequestBody Usuario datos) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado",
                usuarioService.actualizarUsuario(id, datos)));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<Void>> desactivar(@PathVariable Long id) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario desactivado", null));
    }

    @PostMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> cambiarPassword(@PathVariable Long id,
                                                              @RequestParam String nueva) {
        usuarioService.cambiarPassword(id, nueva);
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada", null));
    }
}
