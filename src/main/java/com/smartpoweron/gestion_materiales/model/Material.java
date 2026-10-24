package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad Material (RF05, RF06, RF07, RF08, RF09, P1-RN01).
 * <p>
 * Catálogo único de componentes eléctricos. El stock_actual y stock_minimo
 * se gestionan aquí mismo (piloto contempla un único almacén). Los
 * movimientos de ENTRADA, SALIDA, DEVOLUCION y MERMA se registran en
 * Trazabilidad.
 * <p>
 * Reglas de negocio implementadas:
 * <ul>
 *   <li>P1-RN01: código de fabricante y especificación técnica obligatorios</li>
 *   <li>P1-RN02: el stock solo aumenta tras validar la orden de compra</li>
 *   <li>RF07: alerta visual cuando stock_actual ≤ stock_minimo</li>
 * </ul>
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Long idMaterial;

    /** Código único de fabricante (MLFB / número de parte). P1-RN01. */
    @NotBlank(message = "El código de fabricante es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @NotBlank(message = "El nombre del material es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    /** Especificación técnica obligatoria (P1-RN01). */
    @NotBlank(message = "La especificación técnica es obligatoria")
    @Column(name = "especificacion_tecnica", nullable = false, columnDefinition = "TEXT")
    private String especificacionTecnica;

    @Column(name = "categoria", length = 50)
    private String categoria;

    @Column(name = "marca", length = 50)
    private String marca;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "unidad_medida", length = 20)
    private String unidadMedida;

    @NotNull(message = "El stock actual es obligatorio")
    @Min(value = 0, message = "El stock actual no puede ser negativo")
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    // ----- Ubicación física en el almacén -----
    @Column(name = "pasillo", length = 20)
    private String pasillo;

    @Column(name = "estante", length = 20)
    private String estante;

    @Column(name = "nivel", length = 20)
    private String nivel;

    /** Estado lógico: ACTIVO, INACTIVO */
    @Column(name = "estado", length = 20)
    private String estado;

    /**
     * Verifica si el material está en alerta de stock mínimo (RF07).
     */
    @Transient
    public boolean isStockBajo() {
        return stockActual != null && stockMinimo != null && stockActual <= stockMinimo;
    }
}
