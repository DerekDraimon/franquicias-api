package com.derek.franquicias.infrastructure.mongo.producto;

import com.derek.franquicias.domain.gateway.ProductoGateway;
import com.derek.franquicias.domain.model.Producto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ProductoMongoAdapter implements ProductoGateway {

    private final ProductoMongoRepository repository;

    public ProductoMongoAdapter(ProductoMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Producto> guardar(Producto producto) {
        return repository.save(ProductoMapper.aDocumento(producto))
                .map(ProductoMapper::aDominio);
    }

    @Override
    public Mono<Producto> buscarPorId(String id) {
        return repository.findById(id)
                .map(ProductoMapper::aDominio);
    }

    @Override
    public Mono<Void> eliminar(String id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Producto> buscarMayorStockDeSucursal(String sucursalId) {
        return repository.findFirstBySucursalIdOrderByStockDesc(sucursalId)
                .map(ProductoMapper::aDominio);
    }
}
