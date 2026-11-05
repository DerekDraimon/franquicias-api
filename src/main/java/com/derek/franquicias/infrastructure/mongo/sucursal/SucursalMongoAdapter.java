package com.derek.franquicias.infrastructure.mongo.sucursal;

import com.derek.franquicias.domain.gateway.SucursalGateway;
import com.derek.franquicias.domain.model.Sucursal;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class SucursalMongoAdapter implements SucursalGateway {

    private final SucursalMongoRepository repository;

    public SucursalMongoAdapter(SucursalMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Sucursal> guardar(Sucursal sucursal) {
        return repository.save(SucursalMapper.aDocumento(sucursal))
                .map(SucursalMapper::aDominio);
    }

    @Override
    public Mono<Sucursal> buscarPorId(String id) {
        return repository.findById(id)
                .map(SucursalMapper::aDominio);
    }

    @Override
    public Flux<Sucursal> buscarPorFranquicia(String franquiciaId) {
        return repository.findByFranquiciaId(franquiciaId)
                .map(SucursalMapper::aDominio);
    }
}
