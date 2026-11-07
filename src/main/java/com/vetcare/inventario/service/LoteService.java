package com.vetcare.inventario.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetcare.inventario.dto.LoteResponse;
import com.vetcare.inventario.entity.Lote;
import com.vetcare.inventario.exception.SolicitudInvalidaException;
import com.vetcare.inventario.repository.LoteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public List<LoteResponse> porVencer(int dias) {
        if (dias < 0 || dias > 365) {
            throw new SolicitudInvalidaException("El parámetro dias debe estar entre 0 y 365");
        }
        LocalDate hoy = LocalDate.now();
        return loteRepository
                .findByFechaVencimientoLessThanEqualAndCantidadActualGreaterThanOrderByFechaVencimientoAsc(
                        hoy.plusDays(dias), 0)
                .stream()
                .map(lote -> aRespuesta(lote, hoy))
                .toList();
    }

    private LoteResponse aRespuesta(Lote lote, LocalDate hoy) {
        return new LoteResponse(
                lote.getId(),
                lote.getProducto().getId(),
                lote.getProducto().getNombre(),
                lote.getCodigoLote(),
                lote.getFechaVencimiento(),
                lote.getCantidadActual(),
                ChronoUnit.DAYS.between(hoy, lote.getFechaVencimiento()));
    }
}
