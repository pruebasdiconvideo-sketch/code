package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad ParametroPrueba (RF21, R-CAL-01).
 * <p>
 * Configurado por el Administrador (CU02). Define el rango técnico de
 * tolerancia para cada tipo de prueba (tensión, aislamiento, resistencia, etc.).
 * El sistema valida que valor_minimo ≤ valor_maximo (restricción CHECK en MySQL).
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "parametro_prueba")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroPrueba {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro")
    private Long idParametro;

    /** TENSION, AISLAMIENTO, RESISTENCIA, CONTINUIDAD, etc. */
    @NotBlank(message = "El tipo de prueba es obligatorio")
    @Column(name = "tipo_prueba", nullable = false, length = 50)
    private String tipoPrueba;

    @NotBlank(message = "El nombre del parámetro es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "El valor mínimo es obligatorio")
    @DecimalMin(value = "0.0", message = "El valor mínimo no puede ser negativo")
    @Column(name = "valor_minimo", nullable = false, precision = 10, scale = 3)
    private BigDecimal valorMinimo;

    @NotNull(message = "El valor máximo es obligatorio")
    @DecimalMax(value = "100000.0", message = "El valor máximo es irrazonable")
    @Column(name = "valor_maximo", nullable = false, precision = 10, scale = 3)
    private BigDecimal valorMaximo;

    @Column(name = "unidad", length = 20)
    private String unidad;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    /**
     * Determina si un valor medido cumple con el rango técnico (R-CAL-01).
     */
    public boolean isConforme(BigDecimal valorMedido) {
        if (valorMedido == null || valorMinimo == null || valorMaximo == null) return false;
        return valorMedido.compareTo(valorMinimo) >= 0 && valorMedido.compareTo(valorMaximo) <= 0;
    }
}
