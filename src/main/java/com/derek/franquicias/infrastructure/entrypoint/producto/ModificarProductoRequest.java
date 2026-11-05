package com.derek.franquicias.infrastructure.entrypoint.producto;

// Ambos campos son opcionales: solo se modifica lo que llega en el body.
public record ModificarProductoRequest(String nombre, Integer stock) {
}
