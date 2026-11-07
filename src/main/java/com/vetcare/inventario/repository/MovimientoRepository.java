package com.vetcare.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.vetcare.inventario.entity.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @Query("select m from Movimiento m "
            + "join fetch m.lote l "
            + "join fetch l.producto "
            + "left join fetch m.usuario "
            + "order by m.fecha desc, m.id desc")
    List<Movimiento> findAllConDetalle();
}
