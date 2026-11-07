package com.vetcare.inventario.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vetcare.inventario.dto.ProveedorRequest;
import com.vetcare.inventario.dto.ProveedorResponse;
import com.vetcare.inventario.entity.Proveedor;
import com.vetcare.inventario.exception.RecursoDuplicadoException;
import com.vetcare.inventario.exception.RecursoNoEncontradoException;
import com.vetcare.inventario.repository.ProveedorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Transactional(readOnly = true)
    public List<ProveedorResponse> listar() {
        return proveedorRepository.findByActivoTrueOrderByRazonSocialAsc().stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProveedorResponse obtener(Long id) {
        return aRespuesta(buscarActivo(id));
    }

    @Transactional
    public ProveedorResponse crear(ProveedorRequest request) {
        if (proveedorRepository.existsByRuc(request.ruc())) {
            throw new RecursoDuplicadoException("Ya existe un proveedor con el RUC " + request.ruc());
        }
        Proveedor proveedor = new Proveedor();
        proveedor.setRazonSocial(request.razonSocial().trim());
        proveedor.setRuc(request.ruc());
        proveedor.setTelefono(request.telefono());
        proveedor.setCorreo(request.correo());
        return aRespuesta(proveedorRepository.save(proveedor));
    }

    @Transactional
    public ProveedorResponse actualizar(Long id, ProveedorRequest request) {
        Proveedor proveedor = buscarActivo(id);
        if (proveedorRepository.existsByRucAndIdNot(request.ruc(), id)) {
            throw new RecursoDuplicadoException("Ya existe un proveedor con el RUC " + request.ruc());
        }
        proveedor.setRazonSocial(request.razonSocial().trim());
        proveedor.setRuc(request.ruc());
        proveedor.setTelefono(request.telefono());
        proveedor.setCorreo(request.correo());
        return aRespuesta(proveedor);
    }

    @Transactional
    public void desactivar(Long id) {
        buscarActivo(id).setActivo(false);
    }

    private Proveedor buscarActivo(Long id) {
        return proveedorRepository.findById(id)
                .filter(Proveedor::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el proveedor con id " + id));
    }

    private ProveedorResponse aRespuesta(Proveedor p) {
        return new ProveedorResponse(
                p.getId(), p.getRazonSocial(), p.getRuc(), p.getTelefono(), p.getCorreo(), p.isActivo());
    }
}
