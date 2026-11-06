package com.derek.franquicias.domain.gateway;

import com.derek.franquicias.domain.model.Franquicia;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FranquiciaGateway {

    Mono<Franquicia> guardar(Franquicia franquicia);

    Mono<Franquicia> buscarPorId(String id);

    Flux<Franquicia> listar();
}
