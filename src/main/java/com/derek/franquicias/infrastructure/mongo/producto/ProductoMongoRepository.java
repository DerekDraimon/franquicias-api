package com.derek.franquicias.infrastructure.mongo.producto;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProductoMongoRepository extends ReactiveMongoRepository<ProductoDocument, String> {

    Mono<ProductoDocument> findFirstBySucursalIdOrderByStockDesc(String sucursalId);
}
