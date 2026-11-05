package com.derek.franquicias.infrastructure.entrypoint.producto;

import com.derek.franquicias.domain.model.ProductoMayorStock;

public record ProductoMayorStockResponse(
        String productoId,
        String nombre,
        int stock,
        String sucursalId,
        String sucursalNombre) {

    static ProductoMayorStockResponse desde(ProductoMayorStock producto) {
        return new ProductoMayorStockResponse(
                producto.productoId(),
                producto.nombre(),
                producto.stock(),
                producto.sucursalId(),
                producto.sucursalNombre());
    }
}
