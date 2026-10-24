package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Entidad DetalleDespacho (RF16, P3-RN01).
 * <p>
 * Cada línea de un despacho registra el código digitado por el almacenero
 * (RF16) y la cantidad entregada. El código digitado se contrasta con el
 * código del detalle de solicitud: si no coinciden, el despacho se rechaza.
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "detalle_despacho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleDespacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_despacho")
    private Long idDetalleDespacho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_despacho", nullable = false)
    private Despacho despacho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_detalle_solicitud", nullable = false)
    private DetalleSolicitud detalleSolicitud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_material", nullable = false)
    private Material material;

    /** Código digitado por el almacenero (RF16, P3-RN01). */
    @NotBlank(message = "El código digitado es obligatorio")
    @Column(name = "codigo_digitado", nullable = false, length = 150)
    private String codigoDigitado;

    @NotNull(message = "La cantidad entregada es obligatoria")
    @Min(value = 1, message = "La cantidad entregada debe ser mayor que cero")
    @Column(name = "cantidad_entregada", nullable = false)
    private Integer cantidadEntregada;
}
