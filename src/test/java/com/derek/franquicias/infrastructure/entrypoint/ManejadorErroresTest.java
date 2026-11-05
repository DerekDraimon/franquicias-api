package com.derek.franquicias.infrastructure.entrypoint;

import com.derek.franquicias.domain.exception.DatoInvalidoException;
import com.derek.franquicias.domain.exception.RecursoNoEncontradoException;
import com.derek.franquicias.domain.usecase.FranquiciaUseCase;
import com.derek.franquicias.infrastructure.entrypoint.franquicia.FranquiciaController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

// Usa el controlador de franquicias como ejemplo: el manejador aplica igual a todos.
@WebFluxTest(FranquiciaController.class)
class ManejadorErroresTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FranquiciaUseCase franquiciaUseCase;

    @Test
    void unRecursoQueNoExisteDevuelve404() {
        when(franquiciaUseCase.renombrar("f9", "Otro"))
                .thenReturn(Mono.error(new RecursoNoEncontradoException("No existe la franquicia con id f9")));

        webTestClient.patch().uri("/api/franquicias/f9")
                .header("Content-Type", "application/json")
                .bodyValue("{\"nombre\": \"Otro\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo(404)
                .jsonPath("$.mensaje").isEqualTo("No existe la franquicia con id f9");
    }

    @Test
    void unDatoInvalidoDevuelve400() {
        when(franquiciaUseCase.crear(""))
                .thenReturn(Mono.error(new DatoInvalidoException("El nombre de la franquicia es obligatorio")));

        webTestClient.post().uri("/api/franquicias")
                .header("Content-Type", "application/json")
                .bodyValue("{\"nombre\": \"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo(400)
                .jsonPath("$.mensaje").isEqualTo("El nombre de la franquicia es obligatorio");
    }

    @Test
    void unJsonMalFormadoDevuelve400ConElMismoFormato() {
        webTestClient.post().uri("/api/franquicias")
                .header("Content-Type", "application/json")
                .bodyValue("{ esto no es json")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo(400)
                .jsonPath("$.mensaje").isEqualTo("El cuerpo de la petición no es válido");
    }
}
