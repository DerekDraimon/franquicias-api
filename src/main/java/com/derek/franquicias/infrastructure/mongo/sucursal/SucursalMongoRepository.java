package com.derek.franquicias.infrastructure.mongo.sucursal;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface SucursalMongoRepository extends ReactiveMongoRepository<SucursalDocument, String> {

    Flux<SucursalDocument> findByFranquiciaId(String franquiciaId);
}
