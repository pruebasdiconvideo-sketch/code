package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad MermaDevolucion (RF24, RF25, RF26, RF27, P5-RN01, P5-RN02).
 * <p>
 * Registra devoluciones de insumos sobrantes y bajas por merma.
 * <ul>
 *   <li>Tipo DEVOLUCION: PENDIENTE → REINTEGRADO (verifica el Almacenero)</li>
 *   <li>Tipo MERMA: PENDIENTE → MERMADO (valida el Jefe de Producción)</li>
 * </ul>
 * Una DEVOLUCION en buen estado reintegra stock al MATERIAL y registra un
 * movimiento DEVOLUCION en Trazabilidad. Una MERMA es una baja definitiva que
 * registra un movimiento MERMA; mientras no esté validada, no produce efecto
 * alguno sobre el inventario.
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "merma_devolucion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MermaDevolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_merma")
    private Long idMerma;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proyecto", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_material", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_registra", nullable = false)
    private Usuario usuarioRegistra;

    /** Nullable: solo el Jefe de Producción valida bajas por merma (P5-RN02). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_validador")
    private Usuario usuarioValidador;

    /** DEVOLUCION o MERMA. */
    @NotBlank(message = "El tipo es obligatorio")
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    /** PENDIENTE, REINTEGRADO, MERMADO. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "motivo", length = 300)
    private String motivo;

    @Column(name = "estado_fisico", length = 50)
    private String estadoFisico;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_validacion")
    private LocalDateTime fechaValidacion;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.estado == null) this.estado = "PENDIENTE";
    }
}
