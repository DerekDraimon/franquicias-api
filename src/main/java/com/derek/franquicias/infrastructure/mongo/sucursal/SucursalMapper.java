package com.derek.franquicias.infrastructure.mongo.sucursal;

import com.derek.franquicias.domain.model.Sucursal;

final class SucursalMapper {

    private SucursalMapper() {
    }

    static SucursalDocument aDocumento(Sucursal sucursal) {
        return new SucursalDocument(sucursal.id(), sucursal.nombre(), sucursal.franquiciaId());
    }

    static Sucursal aDominio(SucursalDocument documento) {
        return new Sucursal(documento.id(), documento.nombre(), documento.franquiciaId());
    }
}
