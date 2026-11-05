package com.derek.franquicias.domain.gateway;

import com.derek.franquicias.domain.model.Producto;
import reactor.core.publisher.Mono;

public interface ProductoGateway {

    Mono<Producto> guardar(Producto producto);

    Mono<Producto> buscarPorId(String id);

    Mono<Void> eliminar(String id);

    Mono<Producto> buscarMayorStockDeSucursal(String sucursalId);
}
