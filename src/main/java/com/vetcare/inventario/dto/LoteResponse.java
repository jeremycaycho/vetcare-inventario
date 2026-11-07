package com.vetcare.inventario.dto;

import java.time.LocalDate;

public record LoteResponse(
        Long id,
        Long idProducto,
        String producto,
        String codigoLote,
        LocalDate fechaVencimiento,
        Integer cantidadActual,
        long diasParaVencer) {
}
