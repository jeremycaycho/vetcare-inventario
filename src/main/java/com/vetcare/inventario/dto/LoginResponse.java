package com.vetcare.inventario.dto;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        String usuario,
        String rol) {
}
