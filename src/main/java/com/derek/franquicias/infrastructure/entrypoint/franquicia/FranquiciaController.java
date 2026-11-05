package com.derek.franquicias.infrastructure.entrypoint.franquicia;

import com.derek.franquicias.domain.usecase.FranquiciaUseCase;
import com.derek.franquicias.infrastructure.entrypoint.NombreRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/franquicias")
public class FranquiciaController {

    private final FranquiciaUseCase franquiciaUseCase;

    public FranquiciaController(FranquiciaUseCase franquiciaUseCase) {
        this.franquiciaUseCase = franquiciaUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FranquiciaResponse> crear(@RequestBody NombreRequest request) {
        return franquiciaUseCase.crear(request.nombre())
                .map(FranquiciaResponse::desde);
    }

    @PatchMapping("/{id}")
    public Mono<FranquiciaResponse> renombrar(@PathVariable String id, @RequestBody NombreRequest request) {
        return franquiciaUseCase.renombrar(id, request.nombre())
                .map(FranquiciaResponse::desde);
    }
}
