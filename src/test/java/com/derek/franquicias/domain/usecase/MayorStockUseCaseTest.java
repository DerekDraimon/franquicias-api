package com.derek.franquicias.domain.usecase;

import com.derek.franquicias.domain.exception.RecursoNoEncontradoException;
import com.derek.franquicias.domain.gateway.FranquiciaGateway;
import com.derek.franquicias.domain.gateway.ProductoGateway;
import com.derek.franquicias.domain.gateway.SucursalGateway;
import com.derek.franquicias.domain.model.Franquicia;
import com.derek.franquicias.domain.model.Producto;
import com.derek.franquicias.domain.model.ProductoMayorStock;
import com.derek.franquicias.domain.model.Sucursal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MayorStockUseCaseTest {

    private FranquiciaGateway franquiciaGateway;
    private SucursalGateway sucursalGateway;
    private ProductoGateway productoGateway;
    private MayorStockUseCase useCase;

    @BeforeEach
    void setUp() {
        franquiciaGateway = mock(FranquiciaGateway.class);
        sucursalGateway = mock(SucursalGateway.class);
        productoGateway = mock(ProductoGateway.class);
        useCase = new MayorStockUseCase(franquiciaGateway, sucursalGateway, productoGateway);
    }

    @Test
    void devuelveElProductoConMasStockDeCadaSucursal() {
        when(franquiciaGateway.buscarPorId("f1"))
                .thenReturn(Mono.just(new Franquicia("f1", "Don Mario")));
        when(sucursalGateway.buscarPorFranquicia("f1"))
                .thenReturn(Flux.just(
                        new Sucursal("s1", "Centro", "f1"),
                        new Sucursal("s2", "Norte", "f1")));
        when(productoGateway.buscarMayorStockDeSucursal("s1"))
                .thenReturn(Mono.just(new Producto("p1", "Pizza", 30, "s1")));
        when(productoGateway.buscarMayorStockDeSucursal("s2"))
                .thenReturn(Mono.just(new Producto("p5", "Gaseosa", 80, "s2")));

        StepVerifier.create(useCase.consultarPorFranquicia("f1"))
                .expectNext(new ProductoMayorStock("p1", "Pizza", 30, "s1", "Centro"))
                .expectNext(new ProductoMayorStock("p5", "Gaseosa", 80, "s2", "Norte"))
                .verifyComplete();
    }

    @Test
    void lasSucursalesSinProductosNoAparecen() {
        when(franquiciaGateway.buscarPorId("f1"))
                .thenReturn(Mono.just(new Franquicia("f1", "Don Mario")));
        when(sucursalGateway.buscarPorFranquicia("f1"))
                .thenReturn(Flux.just(
                        new Sucursal("s1", "Centro", "f1"),
                        new Sucursal("s2", "Norte", "f1")));
        when(productoGateway.buscarMayorStockDeSucursal("s1")).thenReturn(Mono.empty());
        when(productoGateway.buscarMayorStockDeSucursal("s2"))
                .thenReturn(Mono.just(new Producto("p5", "Gaseosa", 80, "s2")));

        StepVerifier.create(useCase.consultarPorFranquicia("f1"))
                .expectNext(new ProductoMayorStock("p5", "Gaseosa", 80, "s2", "Norte"))
                .verifyComplete();
    }

    @Test
    void unaFranquiciaSinSucursalesDevuelveUnaListaVacia() {
        when(franquiciaGateway.buscarPorId("f1"))
                .thenReturn(Mono.just(new Franquicia("f1", "Don Mario")));
        when(sucursalGateway.buscarPorFranquicia("f1")).thenReturn(Flux.empty());

        StepVerifier.create(useCase.consultarPorFranquicia("f1"))
                .verifyComplete();
    }

    @Test
    void unaFranquiciaQueNoExisteDevuelveError() {
        when(franquiciaGateway.buscarPorId("f9")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.consultarPorFranquicia("f9"))
                .expectError(RecursoNoEncontradoException.class)
                .verify();

        verify(sucursalGateway, never()).buscarPorFranquicia(anyString());
    }
}
