package com.vetcare.inventario.dto;

public record StockResponse(
        Long idProducto,
        String codigo,
        String nombre,
        Integer stockMinimo,
        Long stockActual) {
}
