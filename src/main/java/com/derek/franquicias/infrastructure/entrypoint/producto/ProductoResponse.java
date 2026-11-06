package com.derek.franquicias.infrastructure.entrypoint.producto;

import com.derek.franquicias.domain.model.Producto;

public record ProductoResponse(String id, String nombre, int stock, String sucursalId) {

    static ProductoResponse desde(Producto producto) {
        return new ProductoResponse(producto.id(), producto.nombre(), producto.stock(), producto.sucursalId());
    }
}
