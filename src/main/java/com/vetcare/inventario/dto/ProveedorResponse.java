package com.vetcare.inventario.dto;

public record ProveedorResponse(
        Long id, String razonSocial, String ruc, String telefono, String correo, boolean activo) {
}
