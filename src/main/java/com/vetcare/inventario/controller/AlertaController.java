package com.vetcare.inventario.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.inventario.dto.AlertaStockResponse;
import com.vetcare.inventario.service.InventarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final InventarioService inventarioService;

    @GetMapping("/stock-bajo")
    public List<AlertaStockResponse> stockBajo() {
        return inventarioService.stockBajo();
    }
}
