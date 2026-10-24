package com.smartpoweron.gestion_materiales.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para registrar una solicitud de materiales con BOM (RF11, RF12).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemBomDto {

    @NotBlank(message = "El código del material es obligatorio")
    private String codigoMaterial;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    private BigDecimal precioUnitario;
}
