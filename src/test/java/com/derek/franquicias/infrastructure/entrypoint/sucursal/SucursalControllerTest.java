package com.derek.franquicias.infrastructure.entrypoint.sucursal;

import com.derek.franquicias.domain.model.Sucursal;
import com.derek.franquicias.domain.usecase.SucursalUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest(SucursalController.class)
class SucursalControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SucursalUseCase sucursalUseCase;

    @Test
    void crearDevuelve201ConLaSucursalCreada() {
        when(sucursalUseCase.crear("f1", "Sede Centro"))
                .thenReturn(Mono.just(new Sucursal("s1", "Sede Centro", "f1")));

        webTestClient.post().uri("/api/franquicias/f1/sucursales")
                .bodyValue("{\"nombre\": \"Sede Centro\"}")
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("s1")
                .jsonPath("$.nombre").isEqualTo("Sede Centro")
                .jsonPath("$.franquiciaId").isEqualTo("f1");
    }

    @Test
    void listarDevuelve200ConLasSucursalesDeLaFranquicia() {
        when(sucursalUseCase.listarPorFranquicia("f1"))
                .thenReturn(Flux.just(new Sucursal("s1", "Centro", "f1"), new Sucursal("s2", "Norte", "f1")));

        webTestClient.get().uri("/api/franquicias/f1/sucursales")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[1].nombre").isEqualTo("Norte")
                .jsonPath("$[1].franquiciaId").isEqualTo("f1");
    }

    @Test
    void renombrarDevuelve200ConElNuevoNombre() {
        when(sucursalUseCase.renombrar("s1", "Sede Norte"))
                .thenReturn(Mono.just(new Sucursal("s1", "Sede Norte", "f1")));

        webTestClient.patch().uri("/api/sucursales/s1")
                .bodyValue("{\"nombre\": \"Sede Norte\"}")
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombre").isEqualTo("Sede Norte");
    }
}
