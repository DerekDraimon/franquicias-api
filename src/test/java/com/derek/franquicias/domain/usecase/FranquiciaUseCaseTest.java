package com.derek.franquicias.domain.usecase;

import com.derek.franquicias.domain.exception.DatoInvalidoException;
import com.derek.franquicias.domain.exception.RecursoNoEncontradoException;
import com.derek.franquicias.domain.gateway.FranquiciaGateway;
import com.derek.franquicias.domain.model.Franquicia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FranquiciaUseCaseTest {

    private FranquiciaGateway franquiciaGateway;
    private FranquiciaUseCase useCase;

    @BeforeEach
    void setUp() {
        franquiciaGateway = mock(FranquiciaGateway.class);
        useCase = new FranquiciaUseCase(franquiciaGateway);
    }

    @Test
    void crearGuardaLaFranquicia() {
        when(franquiciaGateway.guardar(any()))
                .thenReturn(Mono.just(new Franquicia("f1", "Don Mario")));

        StepVerifier.create(useCase.crear("Don Mario"))
                .expectNext(new Franquicia("f1", "Don Mario"))
                .verifyComplete();
    }

    @Test
    void crearConNombreVacioDevuelveErrorYNoGuarda() {
        StepVerifier.create(useCase.crear(""))
                .expectError(DatoInvalidoException.class)
                .verify();

        verify(franquiciaGateway, never()).guardar(any());
    }

    @Test
    void renombrarGuardaElNuevoNombre() {
        when(franquiciaGateway.buscarPorId("f1"))
                .thenReturn(Mono.just(new Franquicia("f1", "Don Mario")));
        when(franquiciaGateway.guardar(any()))
                .thenAnswer(invocacion -> Mono.just(invocacion.getArgument(0)));

        StepVerifier.create(useCase.renombrar("f1", "Don Mario Express"))
                .expectNext(new Franquicia("f1", "Don Mario Express"))
                .verifyComplete();
    }

    @Test
    void renombrarUnaFranquiciaQueNoExisteDevuelveError() {
        when(franquiciaGateway.buscarPorId("f9")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.renombrar("f9", "Otro nombre"))
                .expectError(RecursoNoEncontradoException.class)
                .verify();

        verify(franquiciaGateway, never()).guardar(any());
    }
}
