package com.vetcare.inventario.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.inventario.dto.StockResponse;
import com.vetcare.inventario.service.InventarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final InventarioService inventarioService;

    @GetMapping("/existencias")
    public List<StockResponse> existencias() {
        return inventarioService.stock();
    }
}
