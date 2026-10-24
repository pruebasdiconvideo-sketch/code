package com.smartpoweron.gestion_materiales.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * DTO para crear una solicitud con BOM (RF11, RF14).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudBomDto {

    @NotNull(message = "El ID del proyecto es obligatorio")
    private Long idProyecto;

    @NotEmpty(message = "La lista de materiales (BOM) no puede estar vacía")
    private List<ItemBomDto> items;
}
