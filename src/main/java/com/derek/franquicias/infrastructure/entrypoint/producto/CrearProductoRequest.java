package com.derek.franquicias.infrastructure.entrypoint.producto;

// stock es Integer para poder distinguir "no se envio" de "se envio 0".
public record CrearProductoRequest(String nombre, Integer stock) {

    int stockOCero() {
        return stock == null ? 0 : stock;
    }
}
