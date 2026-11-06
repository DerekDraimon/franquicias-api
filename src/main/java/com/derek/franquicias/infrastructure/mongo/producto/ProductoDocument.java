package com.derek.franquicias.infrastructure.mongo.producto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

// El indice compuesto permite encontrar rapido el producto con mas stock de una sucursal.
@Document("productos")
@CompoundIndex(name = "sucursal_stock", def = "{ 'sucursalId': 1, 'stock': -1 }")
public record ProductoDocument(@Id String id, String nombre, int stock, String sucursalId) {
}
