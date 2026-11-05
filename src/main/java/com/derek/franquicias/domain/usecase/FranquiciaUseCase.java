package com.derek.franquicias.domain.usecase;

import com.derek.franquicias.domain.exception.RecursoNoEncontradoException;
import com.derek.franquicias.domain.gateway.FranquiciaGateway;
import com.derek.franquicias.domain.model.Franquicia;
import reactor.core.publisher.Mono;

public class FranquiciaUseCase {

    private final FranquiciaGateway franquiciaGateway;

    public FranquiciaUseCase(FranquiciaGateway franquiciaGateway) {
        this.franquiciaGateway = franquiciaGateway;
    }

    public Mono<Franquicia> crear(String nombre) {
        // fromCallable hace que el error de validacion viaje dentro del Mono
        // en lugar de lanzarse antes de que exista el flujo.
        return Mono.fromCallable(() -> new Franquicia(null, nombre))
                .flatMap(franquiciaGateway::guardar);
    }

    public Mono<Franquicia> renombrar(String id, String nuevoNombre) {
        return franquiciaGateway.buscarPorId(id)
                .switchIfEmpty(Mono.error(
                        new RecursoNoEncontradoException("No existe la franquicia con id " + id)))
                .map(franquicia -> franquicia.conNombre(nuevoNombre))
                .flatMap(franquiciaGateway::guardar);
    }
}
