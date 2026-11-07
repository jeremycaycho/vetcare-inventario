package com.vetcare.inventario.dto;

import java.time.LocalDateTime;

import com.vetcare.inventario.entity.MotivoMovimiento;
import com.vetcare.inventario.entity.TipoMovimiento;

public record MovimientoResponse(
        Long id,
        TipoMovimiento tipo,
        MotivoMovimiento motivo,
        Integer cantidad,
        LocalDateTime fecha,
        Long idProducto,
        String producto,
        Long idLote,
        String codigoLote,
        String usuario) {
}
