package com.derek.franquicias.domain.gateway;

import com.derek.franquicias.domain.model.Sucursal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SucursalGateway {

    Mono<Sucursal> guardar(Sucursal sucursal);

    Mono<Sucursal> buscarPorId(String id);

    Flux<Sucursal> buscarPorFranquicia(String franquiciaId);
}
