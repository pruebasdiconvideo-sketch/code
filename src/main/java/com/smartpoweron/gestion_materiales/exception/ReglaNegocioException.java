package com.smartpoweron.gestion_materiales.exception;

import lombok.Getter;

/**
 * Excepción lanzada cuando se viola una regla de negocio (P1-RN01, P2-RN02, etc.).
 * Responde con código HTTP 400 Bad Request.
 */
@Getter
public class ReglaNegocioException extends RuntimeException {

    private final String codigoRegla;

    public ReglaNegocioException(String mensaje, String codigoRegla) {
        super(mensaje);
        this.codigoRegla = codigoRegla;
    }

    public ReglaNegocioException(String mensaje) {
        this(mensaje, "RN-GEN");
    }
}
