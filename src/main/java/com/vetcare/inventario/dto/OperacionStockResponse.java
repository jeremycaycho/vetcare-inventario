package com.vetcare.inventario.dto;

import java.util.List;

import com.vetcare.inventario.entity.TipoMovimiento;

public record OperacionStockResponse(
        TipoMovimiento tipo,
        Integer cantidad,
        Long stockActual,
        List<MovimientoResponse> movimientos) {
}
