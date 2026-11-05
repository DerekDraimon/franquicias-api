package com.derek.franquicias.infrastructure.mongo.franquicia;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface FranquiciaMongoRepository extends ReactiveMongoRepository<FranquiciaDocument, String> {
}
