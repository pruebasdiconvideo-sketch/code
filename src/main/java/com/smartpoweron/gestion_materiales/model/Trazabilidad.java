package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad Trazabilidad (RF09, RF17, RF25, RF28, R-INV-07).
 * <p>
 * Registra de solo inserción el histórico de cada movimiento de inventario.
 * Cada movimiento identifica material, tipo_movimiento, cantidad, fecha,
 * usuario responsable, proyecto u orden de compra cuando corresponda.
 * <p>
 * Tipos de movimiento:
 * <ul>
 *   <li>ENTRADA: al validar el ingreso contra OC (R-INV-05, RF06)</li>
 *   <li>SALIDA: al confirmar la recepción conforme (R-INV-01, RF17)</li>
 *   <li>DEVOLUCION: al reintegrar sobrantes (R-INV-06, RF25)</li>
 *   <li>MERMA: al validar la baja de un componente (R-INV-06, RF26)</li>
 * </ul>
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "trazabilidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trazabilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_trazabilidad")
    private Long idTrazabilidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_material", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    /** Nullable: solo se asocia si el movimiento proviene de un proyecto. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto")
    private Proyecto proyecto;

    /** Nullable: solo se asocia a movimientos ENTRADA validados contra OC. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden_compra")
    private OrdenCompra ordenCompra;

    /** ENTRADA, SALIDA, DEVOLUCION, MERMA. */
    @NotBlank
    @Column(name = "tipo_movimiento", nullable = false, length = 30)
    private String tipoMovimiento;

    @NotNull
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "saldo_resultante")
    private Integer saldoResultante;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "fecha_movimiento", nullable = false, updatable = false)
    private LocalDateTime fechaMovimiento;

    /** Referencia opcional al despacho asociado (movimientos SALIDA). */
    @Column(name = "id_despacho_origen")
    private Long idDespachoOrigen;

    @PrePersist
    protected void onCreate() {
        this.fechaMovimiento = LocalDateTime.now();
    }
}
