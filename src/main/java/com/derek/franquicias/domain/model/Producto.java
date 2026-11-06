package com.derek.franquicias.domain.model;

import com.derek.franquicias.domain.exception.DatoInvalidoException;

public record Producto(String id, String nombre, int stock, String sucursalId) {

    public Producto {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre del producto es obligatorio");
        }
        if (stock < 0) {
            throw new DatoInvalidoException("El stock no puede ser negativo");
        }
    }

    public Producto conNombre(String nuevoNombre) {
        return new Producto(id, nuevoNombre, stock, sucursalId);
    }

    public Producto conStock(int nuevoStock) {
        return new Producto(id, nombre, nuevoStock, sucursalId);
    }
}
