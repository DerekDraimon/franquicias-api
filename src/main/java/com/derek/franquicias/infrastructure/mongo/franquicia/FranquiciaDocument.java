package com.derek.franquicias.infrastructure.mongo.franquicia;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("franquicias")
public record FranquiciaDocument(@Id String id, String nombre) {
}
