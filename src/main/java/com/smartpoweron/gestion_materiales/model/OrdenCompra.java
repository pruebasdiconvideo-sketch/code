package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad OrdenCompra (RF06, RF09, R-INV-05).
 * <p>
 * Representa la orden de compra digital contra la que el Almacenero valida el
 * ingreso de materiales. Un material puede ingresar contra varias OC sin
 * perder el historial; la relación se conserva en Trazabilidad.
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "orden_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden_compra")
    private Long idOrdenCompra;

    @NotBlank(message = "El código de la orden de compra es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @NotBlank(message = "El proveedor es obligatorio")
    @Size(max = 150, message = "El proveedor no puede exceder 150 caracteres")
    @Column(name = "proveedor", nullable = false, length = 150)
    private String proveedor;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    /** Estado: PENDIENTE, CONFORME, CERRADA, ANULADA */
    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    @Column(name = "monto_total", precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) this.estado = "PENDIENTE";
    }
}
