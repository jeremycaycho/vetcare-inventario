package com.vetcare.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetcare.inventario.entity.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    List<Proveedor> findByActivoTrueOrderByRazonSocialAsc();

    boolean existsByRuc(String ruc);

    boolean existsByRucAndIdNot(String ruc, Long id);
}
