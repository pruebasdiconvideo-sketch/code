package com.smartpoweron.gestion_materiales.dto;

import lombok.*;

/**
 * Respuesta estándar de la API REST.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean exito;
    private String mensaje;
    private T data;
    private String codigo;

    public static <T> ApiResponse<T> ok(String mensaje, T data) {
        return ApiResponse.<T>builder()
                .exito(true)
                .mensaje(mensaje)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String mensaje, String codigo) {
        return ApiResponse.<T>builder()
                .exito(false)
                .mensaje(mensaje)
                .codigo(codigo)
                .build();
    }
}
