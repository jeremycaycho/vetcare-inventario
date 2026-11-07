package com.vetcare.inventario.dto;

public record AlertaStockResponse(
        Long idProducto,
        String codigo,
        String nombre,
        Integer stockMinimo,
        Long stockActual,
        Long faltante) {
}
