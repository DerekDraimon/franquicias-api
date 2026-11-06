package com.derek.franquicias.domain.usecase;

import com.derek.franquicias.domain.exception.DatoInvalidoException;
import com.derek.franquicias.domain.exception.RecursoNoEncontradoException;
import com.derek.franquicias.domain.gateway.FranquiciaGateway;
import com.derek.franquicias.domain.gateway.SucursalGateway;
import com.derek.franquicias.domain.model.Franquicia;
import com.derek.franquicias.domain.model.Sucursal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SucursalUseCaseTest {

    private SucursalGateway sucursalGateway;
    private FranquiciaGateway franquiciaGateway;
    private SucursalUseCase useCase;

    @BeforeEach
    void setUp() {
        sucursalGateway = mock(SucursalGateway.class);
        franquiciaGateway = mock(FranquiciaGateway.class);
        useCase = new SucursalUseCase(sucursalGateway, franquiciaGateway);
    }

    @Test
    void crearGuardaLaSucursalEnLaFranquicia() {
        when(franquiciaGateway.buscarPorId("f1"))
                .thenReturn(Mono.just(new Franquicia("f1", "Don Mario")));
        when(sucursalGateway.guardar(any()))
                .thenAnswer(invocacion -> {
                    Sucursal sucursal = invocacion.getArgument(0);
                    return Mono.just(new Sucursal("s1", sucursal.nombre(), sucursal.franquiciaId()));
                });

        StepVerifier.create(useCase.crear("f1", "Sede Centro"))
                .expectNext(new Sucursal("s1", "Sede Centro", "f1"))
                .verifyComplete();
    }

    @Test
    void crearEnUnaFranquiciaQueNoExisteDevuelveError() {
        when(franquiciaGateway.buscarPorId("f9")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.crear("f9", "Sede Centro"))
                .expectError(RecursoNoEncontradoException.class)
                .verify();

        verify(sucursalGateway, never()).guardar(any());
    }

    @Test
    void crearConNombreVacioDevuelveErrorYNoGuarda() {
        when(franquiciaGateway.buscarPorId("f1"))
                .thenReturn(Mono.just(new Franquicia("f1", "Don Mario")));

        StepVerifier.create(useCase.crear("f1", " "))
                .expectError(DatoInvalidoException.class)
                .verify();

        verify(sucursalGateway, never()).guardar(any());
    }

    @Test
    void renombrarGuardaElNuevoNombre() {
        when(sucursalGateway.buscarPorId("s1"))
                .thenReturn(Mono.just(new Sucursal("s1", "Centro", "f1")));
        when(sucursalGateway.guardar(any()))
                .thenAnswer(invocacion -> Mono.just(invocacion.getArgument(0)));

        StepVerifier.create(useCase.renombrar("s1", "Sede Centro"))
                .expectNext(new Sucursal("s1", "Sede Centro", "f1"))
                .verifyComplete();
    }

    @Test
    void renombrarUnaSucursalQueNoExisteDevuelveError() {
        when(sucursalGateway.buscarPorId("s9")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.renombrar("s9", "Otro nombre"))
                .expectError(RecursoNoEncontradoException.class)
                .verify();

        verify(sucursalGateway, never()).guardar(any());
    }
}
