package com.smartpoweron.gestion_materiales.controller;

import com.smartpoweron.gestion_materiales.dto.ApiResponse;
import com.smartpoweron.gestion_materiales.dto.LoginRequest;
import com.smartpoweron.gestion_materiales.dto.LoginResponse;
import com.smartpoweron.gestion_materiales.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API REST de autenticación (RF01, RNF03).
 * <p>
 * Endpoint público POST /api/auth/login que valida credenciales contra MySQL
 * y devuelve un JWT firmado HS256.
 *
 * @author Grupo 8 - UTP
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;

    /**
     * RF01 - Inicio de sesión.
     * Devuelve JWT + datos del usuario.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Inicio de sesión exitoso", response));
    }

    /**
     * Verifica si un token JWT es válido (útil para el frontend).
     */
    @GetMapping("/validar")
    public ResponseEntity<ApiResponse<Boolean>> validar(@RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.ok("Token verificado", authService.validarToken(token)));
    }
}
