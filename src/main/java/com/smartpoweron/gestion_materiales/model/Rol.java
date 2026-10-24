package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad Rol (RF02, RF03, OE6).
 * <p>
 * Representa los cinco roles del sistema: ADMINISTRADOR, JEFE_PRODUCCION,
 * ALMACENERO, OPERARIO_ENSAMBLE, INSPECTOR_CALIDAD. No se modela como clases
 * especializadas de Usuario, sino como registros de esta entidad, asociada a
 * Usuario (M:1) y a Permiso (M:N) mediante la tabla asociativa rol_permiso.
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "rol")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "rol_permiso",
            joinColumns = @JoinColumn(name = "id_rol"),
            inverseJoinColumns = @JoinColumn(name = "id_permiso")
    )
    @Builder.Default
    private Set<Permiso> permisos = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) this.activo = true;
    }
}
