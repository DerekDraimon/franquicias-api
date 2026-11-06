package com.derek.franquicias.infrastructure.mongo.franquicia;

import com.derek.franquicias.domain.gateway.FranquiciaGateway;
import com.derek.franquicias.domain.model.Franquicia;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class FranquiciaMongoAdapter implements FranquiciaGateway {

    private final FranquiciaMongoRepository repository;

    public FranquiciaMongoAdapter(FranquiciaMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Franquicia> guardar(Franquicia franquicia) {
        return repository.save(FranquiciaMapper.aDocumento(franquicia))
                .map(FranquiciaMapper::aDominio);
    }

    @Override
    public Mono<Franquicia> buscarPorId(String id) {
        return repository.findById(id)
                .map(FranquiciaMapper::aDominio);
    }

    @Override
    public Flux<Franquicia> listar() {
        return repository.findAll()
                .map(FranquiciaMapper::aDominio);
    }
}
