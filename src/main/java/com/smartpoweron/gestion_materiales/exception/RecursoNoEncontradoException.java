package com.smartpoweron.gestion_materiales.exception;

/**
 * Excepción lanzada cuando un recurso no se encuentra en la base de datos.
 * Responde con código HTTP 404 Not Found.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
