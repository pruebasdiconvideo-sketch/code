package com.smartpoweron.gestion_materiales.dto;

import lombok.*;

/**
 * DTO de respuesta de inicio de sesión (RF01). Contiene el JWT (HS256)
 * y los datos básicos del usuario autenticado.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String tipo;
    private Long idUsuario;
    private String nombreCompleto;
    private String correo;
    private String rol;
    private Long expiraEn;
}
