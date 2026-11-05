package com.derek.franquicias.infrastructure.mongo.sucursal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("sucursales")
public record SucursalDocument(@Id String id, String nombre, @Indexed String franquiciaId) {
}
