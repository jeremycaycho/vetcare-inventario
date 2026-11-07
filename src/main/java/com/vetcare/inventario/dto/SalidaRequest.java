package com.vetcare.inventario.dto;

import com.vetcare.inventario.entity.MotivoMovimiento;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SalidaRequest(
        @NotNull(message = "El producto es obligatorio")
        Long idProducto,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer cantidad,

        @NotNull(message = "El motivo es obligatorio")
        MotivoMovimiento motivo,

        @Size(max = 255, message = "La observación no puede superar 255 caracteres")
        String observacion) {
}
