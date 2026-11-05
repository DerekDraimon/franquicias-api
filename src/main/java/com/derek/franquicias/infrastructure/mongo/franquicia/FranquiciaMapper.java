package com.derek.franquicias.infrastructure.mongo.franquicia;

import com.derek.franquicias.domain.model.Franquicia;

final class FranquiciaMapper {

    private FranquiciaMapper() {
    }

    static FranquiciaDocument aDocumento(Franquicia franquicia) {
        return new FranquiciaDocument(franquicia.id(), franquicia.nombre());
    }

    static Franquicia aDominio(FranquiciaDocument documento) {
        return new Franquicia(documento.id(), documento.nombre());
    }
}
