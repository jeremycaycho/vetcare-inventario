package com.vetcare.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vetcare.inventario.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
