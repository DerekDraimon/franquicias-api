package com.derek.franquicias.domain.usecase;

import com.derek.franquicias.domain.exception.RecursoNoEncontradoException;
import com.derek.franquicias.domain.gateway.FranquiciaGateway;
import com.derek.franquicias.domain.gateway.SucursalGateway;
import com.derek.franquicias.domain.model.Sucursal;
import reactor.core.publisher.Mono;

public class SucursalUseCase {

    private final SucursalGateway sucursalGateway;
    private final FranquiciaGateway franquiciaGateway;

    public SucursalUseCase(SucursalGateway sucursalGateway, FranquiciaGateway franquiciaGateway) {
        this.sucursalGateway = sucursalGateway;
        this.franquiciaGateway = franquiciaGateway;
    }

    public Mono<Sucursal> crear(String franquiciaId, String nombre) {
        return franquiciaGateway.buscarPorId(franquiciaId)
                .switchIfEmpty(Mono.error(
                        new RecursoNoEncontradoException("No existe la franquicia con id " + franquiciaId)))
                .flatMap(franquicia -> sucursalGateway.guardar(new Sucursal(null, nombre, franquicia.id())));
    }

    public Mono<Sucursal> renombrar(String id, String nuevoNombre) {
        return sucursalGateway.buscarPorId(id)
                .switchIfEmpty(Mono.error(
                        new RecursoNoEncontradoException("No existe la sucursal con id " + id)))
                .map(sucursal -> sucursal.conNombre(nuevoNombre))
                .flatMap(sucursalGateway::guardar);
    }
}
