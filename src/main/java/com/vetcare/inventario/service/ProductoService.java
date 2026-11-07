package com.vetcare.inventario.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetcare.inventario.dto.ProductoRequest;
import com.vetcare.inventario.dto.ProductoResponse;
import com.vetcare.inventario.entity.Categoria;
import com.vetcare.inventario.entity.Producto;
import com.vetcare.inventario.exception.RecursoDuplicadoException;
import com.vetcare.inventario.exception.RecursoEnUsoException;
import com.vetcare.inventario.exception.RecursoNoEncontradoException;
import com.vetcare.inventario.repository.CategoriaRepository;
import com.vetcare.inventario.repository.LoteRepository;
import com.vetcare.inventario.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(String nombre, Long idCategoria) {
        boolean hayNombre = nombre != null && !nombre.isBlank();
        List<Producto> productos;
        if (hayNombre && idCategoria != null) {
            productos = productoRepository
                    .findByActivoTrueAndCategoriaIdAndNombreContainingIgnoreCase(idCategoria, nombre.trim());
        } else if (hayNombre) {
            productos = productoRepository.findByActivoTrueAndNombreContainingIgnoreCase(nombre.trim());
        } else if (idCategoria != null) {
            productos = productoRepository.findByActivoTrueAndCategoriaId(idCategoria);
        } else {
            productos = productoRepository.findByActivoTrue();
        }
        return productos.stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return aRespuesta(buscarActivo(id));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        String codigo = request.codigo().trim();
        if (productoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new RecursoDuplicadoException("Ya existe un producto con el código '" + codigo + "'");
        }
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        producto.setNombre(request.nombre().trim());
        producto.setUnidadMedida(request.unidadMedida().trim());
        producto.setPrecio(request.precio());
        producto.setStockMinimo(request.stockMinimo());
        producto.setCategoria(buscarCategoria(request.idCategoria()));
        return aRespuesta(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = buscarActivo(id);
        String codigo = request.codigo().trim();
        if (productoRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new RecursoDuplicadoException("Ya existe un producto con el código '" + codigo + "'");
        }
        producto.setCodigo(codigo);
        producto.setNombre(request.nombre().trim());
        producto.setUnidadMedida(request.unidadMedida().trim());
        producto.setPrecio(request.precio());
        producto.setStockMinimo(request.stockMinimo());
        producto.setCategoria(buscarCategoria(request.idCategoria()));
        return aRespuesta(producto);
    }

    @Transactional
    public void desactivar(Long id) {
        Producto producto = buscarActivo(id);
        if (loteRepository.stockDeProducto(id) > 0) {
            throw new RecursoEnUsoException(
                    "No se puede desactivar el producto porque todavía tiene stock");
        }
        producto.setActivo(false);
    }

    private Producto buscarActivo(Long id) {
        return productoRepository.findById(id)
                .filter(Producto::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto con id " + id));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoría con id " + id));
    }

    private ProductoResponse aRespuesta(Producto p) {
        return new ProductoResponse(
                p.getId(), p.getCodigo(), p.getNombre(), p.getUnidadMedida(), p.getPrecio(),
                p.getStockMinimo(), p.isActivo(),
                p.getCategoria().getId(), p.getCategoria().getNombre());
    }
}
