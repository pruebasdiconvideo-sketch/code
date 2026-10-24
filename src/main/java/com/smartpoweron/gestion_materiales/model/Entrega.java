package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad Entrega (RF17, RF19, R-INV-01, R-INV-03, R-INV-04).
 * <p>
 * Representa la confirmación digital del operario de ensamble de que el kit
 * recibido corresponde con lo solicitado. La relación Despacho-Entrega es
 * 1:0..1 (UNIQUE sobre id_despacho).
 * <p>
 * Al momento de confirmar (Entrega.confirmado = TRUE), el sistema descuenta
 * el stock_actual de cada material, incrementa cantidad_despachada en
 * DetalleSolicitud y registra un movimiento SALIDA en Trazabilidad — todo en
 * la misma transacción @Transactional (R-INV-01).
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "entrega",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_entrega_despacho",
                columnNames = "id_despacho"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrega")
    private Long idEntrega;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_despacho", nullable = false, unique = true)
    private Despacho despacho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_operario", nullable = false)
    private Usuario usuarioOperario;

    @NotNull
    @Column(name = "confirmado", nullable = false)
    private Boolean confirmado;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "observacion", length = 300)
    private String observacion;
}
