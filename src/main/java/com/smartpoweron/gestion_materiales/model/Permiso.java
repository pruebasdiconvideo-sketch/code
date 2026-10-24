package com.smartpoweron.gestion_materiales.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Entidad Permiso.
 * <p>
 * Permisos finos que se asocian a cada rol mediante la tabla rol_permiso.
 * El catálogo inicial contiene 17 permisos que cubren los 28 RF.
 *
 * @author Grupo 8 - UTP
 */
@Entity
@Table(name = "permiso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long idPermiso;

    @NotBlank(message = "El código del permiso es obligatorio")
    @Size(max = 50, message = "El código no puede exceder 50 caracteres")
    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "modulo", length = 50)
    private String modulo;
}
