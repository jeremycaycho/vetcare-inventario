package com.vetcare.inventario.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetcare.inventario.dto.EntradaRequest;
import com.vetcare.inventario.dto.MovimientoResponse;
import com.vetcare.inventario.dto.OperacionStockResponse;
import com.vetcare.inventario.dto.SalidaRequest;
import com.vetcare.inventario.entity.Lote;
import com.vetcare.inventario.entity.MotivoMovimiento;
import com.vetcare.inventario.entity.Movimiento;
import com.vetcare.inventario.entity.Producto;
import com.vetcare.inventario.entity.Proveedor;
import com.vetcare.inventario.entity.TipoMovimiento;
import com.vetcare.inventario.entity.Usuario;
import com.vetcare.inventario.exception.RecursoDuplicadoException;
import com.vetcare.inventario.exception.RecursoNoEncontradoException;
import com.vetcare.inventario.exception.SolicitudInvalidaException;
import com.vetcare.inventario.exception.StockInsuficienteException;
import com.vetcare.inventario.repository.LoteRepository;
import com.vetcare.inventario.repository.MovimientoRepository;
import com.vetcare.inventario.repository.ProductoRepository;
import com.vetcare.inventario.repository.ProveedorRepository;
import com.vetcare.inventario.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public OperacionStockResponse registrarEntrada(EntradaRequest request) {
        Producto producto = productoRepository.findById(request.idProducto())
                .filter(Producto::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el producto con id " + request.idProducto()));
        Proveedor proveedor = proveedorRepository.findById(request.idProveedor())
                .filter(Proveedor::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el proveedor con id " + request.idProveedor()));

        String codigoLote = request.codigoLote().trim();
        if (loteRepository.existsByProductoIdAndCodigoLote(producto.getId(), codigoLote)) {
            throw new RecursoDuplicadoException(
                    "El producto ya tiene un lote con el código '" + codigoLote + "'");
        }

        Lote lote = new Lote();
        lote.setProducto(producto);
        lote.setProveedor(proveedor);
        lote.setCodigoLote(codigoLote);
        lote.setFechaVencimiento(request.fechaVencimiento());
        lote.setCantidadInicial(request.cantidad());
        lote.setCantidadActual(request.cantidad());
        loteRepository.save(lote);

        Movimiento movimiento = registrar(
                TipoMovimiento.ENTRADA, MotivoMovimiento.COMPRA, request.cantidad(), lote, request.observacion());

        Long stockActual = loteRepository.stockDeProducto(producto.getId());
        return new OperacionStockResponse(
                TipoMovimiento.ENTRADA, request.cantidad(), stockActual, List.of(aRespuesta(movimiento)));
    }

    @Transactional
    public OperacionStockResponse registrarSalida(SalidaRequest request) {
        if (request.motivo() == MotivoMovimiento.COMPRA) {
            throw new SolicitudInvalidaException("COMPRA no es un motivo válido para una salida");
        }
        Producto producto = productoRepository.findById(request.idProducto())
                .filter(Producto::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el producto con id " + request.idProducto()));

        // Lotes con existencias, el que vence primero va primero (bloqueados hasta terminar)
        List<Lote> lotes = loteRepository
                .findByProductoIdAndCantidadActualGreaterThanOrderByFechaVencimientoAsc(producto.getId(), 0);
        long disponible = lotes.stream().mapToLong(Lote::getCantidadActual).sum();

        if (request.cantidad() > disponible) {
            throw new StockInsuficienteException(
                    "La cantidad solicitada supera el stock disponible", disponible);
        }

        int pendiente = request.cantidad();
        List<MovimientoResponse> movimientos = new ArrayList<>();
        for (Lote lote : lotes) {
            if (pendiente == 0) {
                break;
            }
            int tomar = Math.min(pendiente, lote.getCantidadActual());
            lote.setCantidadActual(lote.getCantidadActual() - tomar);
            Movimiento movimiento = registrar(
                    TipoMovimiento.SALIDA, request.motivo(), tomar, lote, request.observacion());
            movimientos.add(aRespuesta(movimiento));
            pendiente -= tomar;
        }
        return new OperacionStockResponse(
                TipoMovimiento.SALIDA, request.cantidad(), disponible - request.cantidad(), movimientos);
    }

    @Transactional(readOnly = true)
    public List<MovimientoResponse> listar() {
        return movimientoRepository.findAllConDetalle().stream()
                .map(this::aRespuesta)
                .toList();
    }

    private Movimiento registrar(TipoMovimiento tipo, MotivoMovimiento motivo, int cantidad,
                                 Lote lote, String observacion) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipo(tipo);
        movimiento.setMotivo(motivo);
        movimiento.setCantidad(cantidad);
        movimiento.setLote(lote);
        movimiento.setObservacion(observacion);
        movimiento.setUsuario(usuarioActual());
        return movimientoRepository.save(movimiento);
    }

    private Usuario usuarioActual() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null) {
            return null;
        }
        return usuarioRepository.findByUsername(autenticacion.getName()).orElse(null);
    }

    private MovimientoResponse aRespuesta(Movimiento m) {
        Lote lote = m.getLote();
        Producto producto = lote.getProducto();
        String usuario = m.getUsuario() == null ? null : m.getUsuario().getUsername();
        return new MovimientoResponse(
                m.getId(), m.getTipo(), m.getMotivo(), m.getCantidad(), m.getFecha(),
                producto.getId(), producto.getNombre(), lote.getId(), lote.getCodigoLote(), usuario);
    }
}
