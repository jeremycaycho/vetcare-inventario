package com.vetcare.inventario.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.inventario.dto.EntradaRequest;
import com.vetcare.inventario.dto.MovimientoResponse;
import com.vetcare.inventario.dto.OperacionStockResponse;
import com.vetcare.inventario.dto.SalidaRequest;
import com.vetcare.inventario.service.MovimientoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @PostMapping("/entradas")
    @ResponseStatus(HttpStatus.CREATED)
    public OperacionStockResponse registrarEntrada(@Valid @RequestBody EntradaRequest request) {
        return movimientoService.registrarEntrada(request);
    }

    @PostMapping("/salidas")
    @ResponseStatus(HttpStatus.CREATED)
    public OperacionStockResponse registrarSalida(@Valid @RequestBody SalidaRequest request) {
        return movimientoService.registrarSalida(request);
    }

    @GetMapping
    public List<MovimientoResponse> listar() {
        return movimientoService.listar();
    }
}
