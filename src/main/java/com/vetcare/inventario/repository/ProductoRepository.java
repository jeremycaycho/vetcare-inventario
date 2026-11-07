package com.vetcare.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.vetcare.inventario.dto.StockResponse;
import com.vetcare.inventario.entity.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByCategoriaId(Long idCategoria);

    @EntityGraph(attributePaths = "categoria")
    List<Producto> findByActivoTrue();

    @EntityGraph(attributePaths = "categoria")
    List<Producto> findByActivoTrueAndCategoriaId(Long idCategoria);

    @EntityGraph(attributePaths = "categoria")
    List<Producto> findByActivoTrueAndNombreContainingIgnoreCase(String nombre);

    @EntityGraph(attributePaths = "categoria")
    List<Producto> findByActivoTrueAndCategoriaIdAndNombreContainingIgnoreCase(
            Long idCategoria, String nombre);

    @Query("select new com.vetcare.inventario.dto.StockResponse("
            + "p.id, p.codigo, p.nombre, p.stockMinimo, coalesce(sum(l.cantidadActual), 0L)) "
            + "from Producto p left join Lote l on l.producto = p "
            + "where p.activo = true "
            + "group by p.id, p.codigo, p.nombre, p.stockMinimo "
            + "order by p.nombre")
    List<StockResponse> consultarStock();

    @Query("select new com.vetcare.inventario.dto.StockResponse("
            + "p.id, p.codigo, p.nombre, p.stockMinimo, coalesce(sum(l.cantidadActual), 0L)) "
            + "from Producto p left join Lote l on l.producto = p "
            + "where p.activo = true "
            + "group by p.id, p.codigo, p.nombre, p.stockMinimo "
            + "having coalesce(sum(l.cantidadActual), 0L) < p.stockMinimo "
            + "order by p.nombre")
    List<StockResponse> consultarStockBajo();
}
