package com.derek.franquicias.infrastructure.entrypoint.producto;

import com.derek.franquicias.domain.model.Producto;
import com.derek.franquicias.domain.model.ProductoMayorStock;
import com.derek.franquicias.domain.usecase.MayorStockUseCase;
import com.derek.franquicias.domain.usecase.ProductoUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProductoUseCase productoUseCase;

    @MockitoBean
    private MayorStockUseCase mayorStockUseCase;

    @Test
    void crearDevuelve201ConElProductoCreado() {
        when(productoUseCase.crear("s1", "Pizza", 10))
                .thenReturn(Mono.just(new Producto("p1", "Pizza", 10, "s1")));

        webTestClient.post().uri("/api/sucursales/s1/productos")
                .header("Content-Type", "application/json")
                .bodyValue("{\"nombre\": \"Pizza\", \"stock\": 10}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("p1")
                .jsonPath("$.nombre").isEqualTo("Pizza")
                .jsonPath("$.stock").isEqualTo(10)
                .jsonPath("$.sucursalId").isEqualTo("s1");
    }

    @Test
    void crearSinStockLoCreaConStockCero() {
        when(productoUseCase.crear("s1", "Pizza", 0))
                .thenReturn(Mono.just(new Producto("p1", "Pizza", 0, "s1")));

        webTestClient.post().uri("/api/sucursales/s1/productos")
                .header("Content-Type", "application/json")
                .bodyValue("{\"nombre\": \"Pizza\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(0);

        verify(productoUseCase).crear("s1", "Pizza", 0);
    }

    @Test
    void modificarSoloElStockDevuelve200() {
        when(productoUseCase.modificar("p1", null, 50))
                .thenReturn(Mono.just(new Producto("p1", "Pizza", 50, "s1")));

        webTestClient.patch().uri("/api/productos/p1")
                .header("Content-Type", "application/json")
                .bodyValue("{\"stock\": 50}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(50)
                .jsonPath("$.nombre").isEqualTo("Pizza");

        verify(productoUseCase).modificar("p1", null, 50);
    }

    @Test
    void modificarSoloElNombreDevuelve200() {
        when(productoUseCase.modificar("p1", "Pizza hawaiana", null))
                .thenReturn(Mono.just(new Producto("p1", "Pizza hawaiana", 10, "s1")));

        webTestClient.patch().uri("/api/productos/p1")
                .header("Content-Type", "application/json")
                .bodyValue("{\"nombre\": \"Pizza hawaiana\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombre").isEqualTo("Pizza hawaiana");
    }

    @Test
    void eliminarDevuelve204SinCuerpo() {
        when(productoUseCase.eliminar("p1")).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/productos/p1")
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();

        verify(productoUseCase).eliminar("p1");
    }

    @Test
    void mayorStockDevuelveUnaListaConLaSucursalDeCadaProducto() {
        when(mayorStockUseCase.consultarPorFranquicia("f1"))
                .thenReturn(Flux.just(
                        new ProductoMayorStock("p1", "Pizza", 30, "s1", "Centro"),
                        new ProductoMayorStock("p5", "Gaseosa", 80, "s2", "Norte")));

        webTestClient.get().uri("/api/franquicias/f1/productos/mayor-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].productoId").isEqualTo("p1")
                .jsonPath("$[0].sucursalNombre").isEqualTo("Centro")
                .jsonPath("$[1].nombre").isEqualTo("Gaseosa")
                .jsonPath("$[1].stock").isEqualTo(80)
                .jsonPath("$[1].sucursalId").isEqualTo("s2");
    }
}
