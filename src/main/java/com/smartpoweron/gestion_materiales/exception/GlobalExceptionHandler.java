package com.smartpoweron.gestion_materiales.exception;

import com.smartpoweron.gestion_materiales.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones (RNF09).
 * <p>
 * Convierte cualquier excepción lanzada por los servicios o controladores en
 * una respuesta JSON consistente, incluyendo el código de regla de negocio
 * violada y la marca de tiempo.
 *
 * @author Grupo 8 - UTP
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiResponse<Object>> handleReglaNegocio(ReglaNegocioException ex,
                                                                  HttpServletRequest request) {
        log.warn("Regla de negocio violada: {} - {} (ruta: {})",
                ex.getCodigoRegla(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), ex.getCodigoRegla()));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Object>> handleRecursoNoEncontrado(RecursoNoEncontradoException ex,
                                                                         HttpServletRequest request) {
        log.warn("Recurso no encontrado: {} (ruta: {})", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), "NOT_FOUND"));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        String mensaje = errores.isEmpty()
                ? "Existen campos vacíos, por favor ingresa información"
                : errores.values().iterator().next();
        log.warn("Validación de formulario fallida: {}", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .exito(false)
                        .mensaje(mensaje)
                        .data(errores)
                        .codigo("VALIDATION_ERROR")
                        .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccesoDenegado(AccessDeniedException ex,
                                                                    HttpServletRequest request) {
        log.warn("Acceso denegado a {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("No tienes permisos para acceder a este recurso", "FORBIDDEN"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAutenticacion(AuthenticationException ex) {
        log.warn("Error de autenticación: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Usuario o contraseña incorrectos", "UNAUTHORIZED"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenerico(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "Ocurrió un error inesperado. Contacte al administrador.",
                        "INTERNAL_ERROR"));
    }

    /**
     * Helper para construir respuestas de error con marca de tiempo (RNF09).
     */
    private Map<String, Object> construirErrorBody(String mensaje, String codigo, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("exito", false);
        body.put("mensaje", mensaje);
        body.put("codigo", codigo);
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("ruta", request.getRequestURI());
        return body;
    }
}
