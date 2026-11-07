package com.vetcare.inventario.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vetcare.inventario.dto.LoteResponse;
import com.vetcare.inventario.service.LoteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
public class LoteController {

    private final LoteService loteService;

    @GetMapping("/por-vencer")
    public List<LoteResponse> porVencer(@RequestParam(name = "dias", defaultValue = "30") int dias) {
        return loteService.porVencer(dias);
    }
}
