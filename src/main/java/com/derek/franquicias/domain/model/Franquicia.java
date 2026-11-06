package com.derek.franquicias.domain.model;

import com.derek.franquicias.domain.exception.DatoInvalidoException;

public record Franquicia(String id, String nombre) {

    public Franquicia {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre de la franquicia es obligatorio");
        }
    }

    public Franquicia conNombre(String nuevoNombre) {
        return new Franquicia(id, nuevoNombre);
    }
}
