package com.vetcare.inventario.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vetcare.inventario.entity.Lote;

import jakarta.persistence.LockModeType;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    boolean existsByProductoIdAndCodigoLote(Long idProducto, String codigoLote);

    boolean existsByProveedorId(Long idProveedor);

    /** Lotes con existencias de un producto, el que vence primero va primero. Bloquea las filas. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Lote> findByProductoIdAndCantidadActualGreaterThanOrderByFechaVencimientoAsc(
            Long idProducto, Integer minimo);

    /** Lotes con existencias que vencen hasta la fecha límite (incluye los ya vencidos). */
    @EntityGraph(attributePaths = "producto")
    List<Lote> findByFechaVencimientoLessThanEqualAndCantidadActualGreaterThanOrderByFechaVencimientoAsc(
            LocalDate limite, Integer minimo);

    @Query("select coalesce(sum(l.cantidadActual), 0L) from Lote l where l.producto.id = :idProducto")
    Long stockDeProducto(@Param("idProducto") Long idProducto);
}
