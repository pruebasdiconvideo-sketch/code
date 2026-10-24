package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * Entidad DetalleSolicitud (RF11, RF12, R-INV-02).
 * <p>
 * Cada línea de la solicitud constituye una entrada de la BOM. La columna
 * cantidad_despachada se acumula conforme se confirman despachos; la
 * pendiente se calcula como cantidad_solicitada - cantidad_despachada.
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "detalle_solicitud",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_detalle_solicitud",
                columnNames = {"id_solicitud", "id_material"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_solicitud")
    private Long idDetalleSolicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_solicitud", nullable = false)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_material", nullable = false)
    private Material material;

    @NotNull(message = "La cantidad solicitada es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    @Column(name = "cantidad_solicitada", nullable = false)
    private Integer cantidadSolicitada;

    /** Acumulada: cuántas unidades ya fueron entregadas y confirmadas. */
    @Column(name = "cantidad_despachada", nullable = false)
    private Integer cantidadDespachada;

    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Transient
    public Integer getCantidadPendiente() {
        return (cantidadSolicitada != null ? cantidadSolicitada : 0)
             - (cantidadDespachada != null ? cantidadDespachada : 0);
    }

    @Transient
    public boolean isCompletamenteDespachada() {
        return getCantidadPendiente() <= 0;
    }
}
