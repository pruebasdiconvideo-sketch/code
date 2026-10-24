package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad PruebaCalidad (RF20, RF21, RF22, RF23, R-CAL-01, R-CAL-02, R-CAL-03).
 * <p>
 * Registra cada medición del Técnico de Pruebas (sin cuenta) tomada por el
 * Inspector de Calidad. El resultado se calcula automáticamente a partir del
 * rango del parámetro (R-CAL-01). Si el resultado es NO_CONFORME, el sistema
 * registra una orden de observación (R-CAL-03, RF23). Si todas las pruebas
 * vigentes son CONFORME, se emite el certificado PDF (RF22, R-CAL-02).
 * <p>
 * Los registros no se editan ni eliminan (R-CAL-04): tras una corrección se
 * inserta una nueva fila, conservando el historial completo.
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "prueba_calidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PruebaCalidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prueba")
    private Long idPrueba;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proyecto", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_parametro", nullable = false)
    private ParametroPrueba parametro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_inspector", nullable = false)
    private Usuario usuarioInspector;

    @NotNull(message = "El valor medido es obligatorio")
    @DecimalMin(value = "0.0", message = "El valor medido no puede ser negativo")
    @Column(name = "valor_medido", nullable = false, precision = 10, scale = 3)
    private BigDecimal valorMedido;

    /** CONFORME, NO_CONFORME (calculado por el servicio). */
    @Column(name = "resultado", nullable = false, length = 20)
    private String resultado;

    @Column(name = "observacion", length = 500)
    private String observacion;

    /** Indica si la prueba está vigente (la última para su tipo) - si se inserta otra, esta pasa a false. */
    @Column(name = "vigente")
    private Boolean vigente;

    @Column(name = "fecha_prueba", nullable = false, updatable = false)
    private LocalDateTime fechaPrueba;

    @PrePersist
    protected void onCreate() {
        this.fechaPrueba = LocalDateTime.now();
        if (this.vigente == null) this.vigente = true;
    }
}
