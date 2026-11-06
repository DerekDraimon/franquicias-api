package com.derek.franquicias.infrastructure.entrypoint.franquicia;

import com.derek.franquicias.domain.model.Franquicia;
import com.derek.franquicias.domain.usecase.FranquiciaUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WebFluxTest(FranquiciaController.class)
class FranquiciaControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FranquiciaUseCase franquiciaUseCase;

    @Test
    void crearDevuelve201ConLaFranquiciaCreada() {
        when(franquiciaUseCase.crear("Don Mario"))
                .thenReturn(Mono.just(new Franquicia("f1", "Don Mario")));

        webTestClient.post().uri("/api/franquicias")
                .bodyValue("{\"nombre\": \"Don Mario\"}")
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("f1")
                .jsonPath("$.nombre").isEqualTo("Don Mario");
    }

    @Test
    void listarDevuelve200ConTodasLasFranquicias() {
        when(franquiciaUseCase.listar())
                .thenReturn(Flux.just(new Franquicia("f1", "Don Mario"), new Franquicia("f2", "Otra")));

        webTestClient.get().uri("/api/franquicias")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo("f1")
                .jsonPath("$[1].nombre").isEqualTo("Otra");
    }

    @Test
    void renombrarDevuelve200ConElNuevoNombre() {
        when(franquiciaUseCase.renombrar("f1", "Don Mario Express"))
                .thenReturn(Mono.just(new Franquicia("f1", "Don Mario Express")));

        webTestClient.patch().uri("/api/franquicias/f1")
                .bodyValue("{\"nombre\": \"Don Mario Express\"}")
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("f1")
                .jsonPath("$.nombre").isEqualTo("Don Mario Express");
    }
}
