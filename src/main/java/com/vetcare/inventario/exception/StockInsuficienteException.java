package com.vetcare.inventario.exception;

public class StockInsuficienteException extends RuntimeException {

    private final long stockDisponible;

    public StockInsuficienteException(String mensaje, long stockDisponible) {
        super(mensaje);
        this.stockDisponible = stockDisponible;
    }

    public long getStockDisponible() {
        return stockDisponible;
    }
}
