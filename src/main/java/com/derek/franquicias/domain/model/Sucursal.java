package com.derek.franquicias.domain.model;

import com.derek.franquicias.domain.exception.DatoInvalidoException;

public record Sucursal(String id, String nombre, String franquiciaId) {

    public Sucursal {
        if (nombre == null || nombre.isBlank()) {
            throw new DatoInvalidoException("El nombre de la sucursal es obligatorio");
        }
    }

    public Sucursal conNombre(String nuevoNombre) {
        return new Sucursal(id, nuevoNombre, franquiciaId);
    }
}
