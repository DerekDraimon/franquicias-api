package com.derek.franquicias.infrastructure.mongo.producto;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductoMongoRepository extends ReactiveMongoRepository<ProductoDocument, String> {

    Flux<ProductoDocument> findBySucursalId(String sucursalId);

    Mono<ProductoDocument> findFirstBySucursalIdOrderByStockDesc(String sucursalId);
}
