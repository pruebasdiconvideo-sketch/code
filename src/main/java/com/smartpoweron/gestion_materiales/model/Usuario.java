package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad Usuario (RF01, RF03, RF04, OE6).
 * <p>
 * Representa a las personas autenticadas del sistema. Cada usuario tiene un
 * único rol de los cinco definidos. La contraseña se almacena cifrada con
 * BCrypt (RNF04) en la columna password_hash.
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
    @Column(name = "correo", nullable = false, unique = true, length = 150)
    private String correo;

    /** Hash BCrypt (rounds=10). Nunca se almacena en texto plano (RNF04). */
    @NotBlank(message = "El hash de contraseña es obligatorio")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.activo == null) this.activo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }
}
