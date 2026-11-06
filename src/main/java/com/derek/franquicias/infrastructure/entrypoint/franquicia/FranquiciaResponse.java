package com.derek.franquicias.infrastructure.entrypoint.franquicia;

import com.derek.franquicias.domain.model.Franquicia;

public record FranquiciaResponse(String id, String nombre) {

    static FranquiciaResponse desde(Franquicia franquicia) {
        return new FranquiciaResponse(franquicia.id(), franquicia.nombre());
    }
}
