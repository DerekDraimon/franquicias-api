package com.derek.franquicias.infrastructure.entrypoint.sucursal;

import com.derek.franquicias.domain.usecase.SucursalUseCase;
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
@RequestMapping("/api")
public class SucursalController {

    private final SucursalUseCase sucursalUseCase;

    public SucursalController(SucursalUseCase sucursalUseCase) {
        this.sucursalUseCase = sucursalUseCase;
    }

    @PostMapping("/franquicias/{franquiciaId}/sucursales")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SucursalResponse> crear(@PathVariable String franquiciaId, @RequestBody NombreRequest request) {
        return sucursalUseCase.crear(franquiciaId, request.nombre())
                .map(SucursalResponse::desde);
    }

    @PatchMapping("/sucursales/{id}")
    public Mono<SucursalResponse> renombrar(@PathVariable String id, @RequestBody NombreRequest request) {
        return sucursalUseCase.renombrar(id, request.nombre())
                .map(SucursalResponse::desde);
    }
}
