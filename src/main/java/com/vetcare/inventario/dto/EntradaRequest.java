package com.vetcare.inventario.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EntradaRequest(
        @NotNull(message = "El producto es obligatorio")
        Long idProducto,

        @NotNull(message = "El proveedor es obligatorio")
        Long idProveedor,

        @NotBlank(message = "El código de lote es obligatorio")
        @Size(max = 50, message = "El código de lote no puede superar 50 caracteres")
        String codigoLote,

        @NotNull(message = "La fecha de vencimiento es obligatoria")
        @FutureOrPresent(message = "La fecha de vencimiento no puede estar en el pasado")
        LocalDate fechaVencimiento,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer cantidad,

        @Size(max = 255, message = "La observación no puede superar 255 caracteres")
        String observacion) {
}
