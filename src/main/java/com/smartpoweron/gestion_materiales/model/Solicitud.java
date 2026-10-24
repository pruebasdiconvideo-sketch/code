package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Solicitud (RF11, RF12, RF13, RF14, P2-RN02).
 * <p>
 * Representa una solicitud de materiales cargada por el Jefe de Producción
 * con la BOM aprobada. El sistema valida que cada componente exista en el
 * catálogo (RF12), verifica la disponibilidad de stock (RF13) y retiene
 * la solicitud si excede el presupuesto (P2-RN02).
 * <p>
 * Estados: BORRADOR, LISTA_DESPACHO, RETENIDA, DESPACHADA, ANULADA.
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "solicitud")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long idSolicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proyecto", nullable = false)
    private Proyecto proyecto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_solicitante", nullable = false)
    private Usuario usuarioSolicitante;

    /** Nullable: solo se llena cuando la solicitud retenida es aprobada por el Administrador. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_aprobador")
    private Usuario usuarioAprobador;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "costo_total", precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Column(name = "motivo_retencion", length = 200)
    private String motivoRetencion;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetalleSolicitud> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaSolicitud = LocalDateTime.now();
        if (this.estado == null) this.estado = "BORRADOR";
        if (this.costoTotal == null) this.costoTotal = BigDecimal.ZERO;
    }

    public void addDetalle(DetalleSolicitud detalle) {
        detalles.add(detalle);
        detalle.setSolicitud(this);
    }
}
