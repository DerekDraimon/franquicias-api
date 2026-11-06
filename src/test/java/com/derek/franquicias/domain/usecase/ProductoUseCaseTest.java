package com.derek.franquicias.domain.usecase;

import com.derek.franquicias.domain.exception.DatoInvalidoException;
import com.derek.franquicias.domain.exception.RecursoNoEncontradoException;
import com.derek.franquicias.domain.gateway.ProductoGateway;
import com.derek.franquicias.domain.gateway.SucursalGateway;
import com.derek.franquicias.domain.model.Producto;
import com.derek.franquicias.domain.model.Sucursal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductoUseCaseTest {

    private ProductoGateway productoGateway;
    private SucursalGateway sucursalGateway;
    private ProductoUseCase useCase;

    @BeforeEach
    void setUp() {
        productoGateway = mock(ProductoGateway.class);
        sucursalGateway = mock(SucursalGateway.class);
        useCase = new ProductoUseCase(productoGateway, sucursalGateway);
    }

    private void productoExistente() {
        when(productoGateway.buscarPorId("p1"))
                .thenReturn(Mono.just(new Producto("p1", "Pizza", 10, "s1")));
    }

    private void guardarDevuelveLoRecibido() {
        when(productoGateway.guardar(any()))
                .thenAnswer(invocacion -> Mono.just(invocacion.getArgument(0)));
    }

    // crear

    @Test
    void crearGuardaElProductoEnLaSucursal() {
        when(sucursalGateway.buscarPorId("s1"))
                .thenReturn(Mono.just(new Sucursal("s1", "Centro", "f1")));
        guardarDevuelveLoRecibido();

        StepVerifier.create(useCase.crear("s1", "Pizza", 10))
                .expectNext(new Producto(null, "Pizza", 10, "s1"))
                .verifyComplete();
    }

    @Test
    void crearEnUnaSucursalQueNoExisteDevuelveError() {
        when(sucursalGateway.buscarPorId("s9")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.crear("s9", "Pizza", 10))
                .expectError(RecursoNoEncontradoException.class)
                .verify();

        verify(productoGateway, never()).guardar(any());
    }

    @Test
    void crearConStockNegativoDevuelveErrorYNoGuarda() {
        when(sucursalGateway.buscarPorId("s1"))
                .thenReturn(Mono.just(new Sucursal("s1", "Centro", "f1")));

        StepVerifier.create(useCase.crear("s1", "Pizza", -5))
                .expectError(DatoInvalidoException.class)
                .verify();

        verify(productoGateway, never()).guardar(any());
    }

    // listar

    @Test
    void listarPorSucursalDevuelveSusProductos() {
        when(sucursalGateway.buscarPorId("s1"))
                .thenReturn(Mono.just(new Sucursal("s1", "Centro", "f1")));
        when(productoGateway.buscarPorSucursal("s1"))
                .thenReturn(Flux.just(new Producto("p1", "Pizza", 10, "s1"), new Producto("p2", "Gaseosa", 5, "s1")));

        StepVerifier.create(useCase.listarPorSucursal("s1"))
                .expectNext(new Producto("p1", "Pizza", 10, "s1"))
                .expectNext(new Producto("p2", "Gaseosa", 5, "s1"))
                .verifyComplete();
    }

    @Test
    void listarPorUnaSucursalQueNoExisteDevuelveError() {
        when(sucursalGateway.buscarPorId("s9")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.listarPorSucursal("s9"))
                .expectError(RecursoNoEncontradoException.class)
                .verify();

        verify(productoGateway, never()).buscarPorSucursal(anyString());
    }

    // modificar

    @Test
    void modificarSoloElStockMantieneElNombre() {
        productoExistente();
        guardarDevuelveLoRecibido();

        StepVerifier.create(useCase.modificar("p1", null, 50))
                .expectNext(new Producto("p1", "Pizza", 50, "s1"))
                .verifyComplete();
    }

    @Test
    void modificarSoloElNombreMantieneElStock() {
        productoExistente();
        guardarDevuelveLoRecibido();

        StepVerifier.create(useCase.modificar("p1", "Pizza hawaiana", null))
                .expectNext(new Producto("p1", "Pizza hawaiana", 10, "s1"))
                .verifyComplete();
    }

    @Test
    void modificarSinNingunCampoDevuelveErrorYNoConsulta() {
        StepVerifier.create(useCase.modificar("p1", null, null))
                .expectError(DatoInvalidoException.class)
                .verify();

        verify(productoGateway, never()).buscarPorId(anyString());
    }

    @Test
    void modificarConStockNegativoDevuelveErrorYNoGuarda() {
        productoExistente();

        StepVerifier.create(useCase.modificar("p1", null, -1))
                .expectError(DatoInvalidoException.class)
                .verify();

        verify(productoGateway, never()).guardar(any());
    }

    @Test
    void modificarUnProductoQueNoExisteDevuelveError() {
        when(productoGateway.buscarPorId("p9")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.modificar("p9", null, 50))
                .expectError(RecursoNoEncontradoException.class)
                .verify();

        verify(productoGateway, never()).guardar(any());
    }

    // eliminar

    @Test
    void eliminarBorraElProducto() {
        productoExistente();
        when(productoGateway.eliminar("p1")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.eliminar("p1"))
                .verifyComplete();

        verify(productoGateway).eliminar("p1");
    }

    @Test
    void eliminarUnProductoQueNoExisteDevuelveError() {
        when(productoGateway.buscarPorId("p9")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.eliminar("p9"))
                .expectError(RecursoNoEncontradoException.class)
                .verify();

        verify(productoGateway, never()).eliminar(anyString());
    }
}
