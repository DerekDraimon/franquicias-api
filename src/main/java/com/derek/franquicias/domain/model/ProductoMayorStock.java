package com.derek.franquicias.domain.model;

public record ProductoMayorStock(
        String productoId,
        String nombre,
        int stock,
        String sucursalId,
        String sucursalNombre) {
}
