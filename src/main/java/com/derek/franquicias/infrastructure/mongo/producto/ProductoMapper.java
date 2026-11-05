package com.derek.franquicias.infrastructure.mongo.producto;

import com.derek.franquicias.domain.model.Producto;

final class ProductoMapper {

    private ProductoMapper() {
    }

    static ProductoDocument aDocumento(Producto producto) {
        return new ProductoDocument(producto.id(), producto.nombre(), producto.stock(), producto.sucursalId());
    }

    static Producto aDominio(ProductoDocument documento) {
        return new Producto(documento.id(), documento.nombre(), documento.stock(), documento.sucursalId());
    }
}
