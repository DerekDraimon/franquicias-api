package com.derek.franquicias.domain.usecase;

import com.derek.franquicias.domain.exception.RecursoNoEncontradoException;
import com.derek.franquicias.domain.gateway.FranquiciaGateway;
import com.derek.franquicias.domain.gateway.ProductoGateway;
import com.derek.franquicias.domain.gateway.SucursalGateway;
import com.derek.franquicias.domain.model.ProductoMayorStock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MayorStockUseCase {

    private final FranquiciaGateway franquiciaGateway;
    private final SucursalGateway sucursalGateway;
    private final ProductoGateway productoGateway;

    public MayorStockUseCase(FranquiciaGateway franquiciaGateway,
                             SucursalGateway sucursalGateway,
                             ProductoGateway productoGateway) {
        this.franquiciaGateway = franquiciaGateway;
        this.sucursalGateway = sucursalGateway;
        this.productoGateway = productoGateway;
    }

    public Flux<ProductoMayorStock> consultarPorFranquicia(String franquiciaId) {
        return franquiciaGateway.buscarPorId(franquiciaId)
                .switchIfEmpty(Mono.error(
                        new RecursoNoEncontradoException("No existe la franquicia con id " + franquiciaId)))
                .flatMapMany(franquicia -> sucursalGateway.buscarPorFranquicia(franquicia.id()))
                // Consulta todas las sucursales al mismo tiempo, pero entrega
                // los resultados en el mismo orden en que llegaron las sucursales.
                .flatMapSequential(sucursal -> productoGateway.buscarMayorStockDeSucursal(sucursal.id())
                        .map(producto -> new ProductoMayorStock(
                                producto.id(),
                                producto.nombre(),
                                producto.stock(),
                                sucursal.id(),
                                sucursal.nombre())));
    }
}
