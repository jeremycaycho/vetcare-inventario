package com.vetcare.inventario.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetcare.inventario.dto.AlertaStockResponse;
import com.vetcare.inventario.dto.StockResponse;
import com.vetcare.inventario.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<StockResponse> stock() {
        return productoRepository.consultarStock();
    }

    @Transactional(readOnly = true)
    public List<AlertaStockResponse> stockBajo() {
        return productoRepository.consultarStockBajo().stream()
                .map(s -> new AlertaStockResponse(
                        s.idProducto(), s.codigo(), s.nombre(), s.stockMinimo(), s.stockActual(),
                        s.stockMinimo() - s.stockActual()))
                .toList();
    }
}
