package com.vetcare.inventario.dto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String codigo,
        String nombre,
        String unidadMedida,
        BigDecimal precio,
        Integer stockMinimo,
        boolean activo,
        Long idCategoria,
        String categoria) {
}
