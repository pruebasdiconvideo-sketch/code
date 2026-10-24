package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad Proyecto (RF10, RF11, RF14).
 * <p>
 * Representa cada proyecto de armado de tablero eléctrico. Se le asigna un
 * código único, cliente, presupuesto y fecha de inicio. Una solicitud puede
 * exceder el presupuesto y quedar retenida para aprobación del Administrador.
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "proyecto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proyecto")
    private Long idProyecto;

    @NotBlank(message = "El código del proyecto es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @NotBlank(message = "El cliente es obligatorio")
    @Size(max = 150, message = "El cliente no puede exceder 150 caracteres")
    @Column(name = "cliente", nullable = false, length = 150)
    private String cliente;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "presupuesto", nullable = false, precision = 12, scale = 2)
    private BigDecimal presupuesto;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) this.estado = "BORRADOR";
    }
}
