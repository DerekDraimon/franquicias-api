package com.derek.franquicias.infrastructure.entrypoint.sucursal;

import com.derek.franquicias.domain.model.Sucursal;

public record SucursalResponse(String id, String nombre, String franquiciaId) {

    static SucursalResponse desde(Sucursal sucursal) {
        return new SucursalResponse(sucursal.id(), sucursal.nombre(), sucursal.franquiciaId());
    }
}
