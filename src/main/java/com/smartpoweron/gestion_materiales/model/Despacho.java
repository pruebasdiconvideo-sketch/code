package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Despacho (RF15, RF16, RF17, RF18, RF19, P3-RN01, P3-RN02).
 * <p>
 * Representa la salida de un kit de materiales hacia el operario de ensamble.
 * El stock_actual NO se modifica al crear el despacho (R-INV-01); solo se
 * descuenta cuando el operario confirma la recepción conforme (Entrega).
 * <p>
 * Tipos: COMPLETO, PARCIAL (P3-RN02).
 * Estados: PREPARANDO, CONFIRMADO, ANULADO.
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "despacho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_despacho")
    private Long idDespacho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_almacenero", nullable = false)
    private Usuario usuarioAlmacenero;

    /** COMPLETO o PARCIAL (P3-RN02). */
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    /** PREPARANDO, CONFIRMADO, ANULADO. */
    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "observacion", length = 300)
    private String observacion;

    @Column(name = "fecha_despacho", nullable = false, updatable = false)
    private LocalDateTime fechaDespacho;

    @OneToMany(mappedBy = "despacho", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleDespacho> detalles = new ArrayList<>();

    @OneToOne(mappedBy = "despacho", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Entrega entrega;

    @PrePersist
    protected void onCreate() {
        this.fechaDespacho = LocalDateTime.now();
        if (this.estado == null) this.estado = "PREPARANDO";
    }
}
