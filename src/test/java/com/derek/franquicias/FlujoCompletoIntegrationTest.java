package com.derek.franquicias;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.util.Map;

// Levanta la aplicacion completa contra un MongoDB real y recorre la API por HTTP.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class FlujoCompletoIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8");

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void flujoCompletoDeLaPrueba() {
        String franquicia = crear("/api/franquicias", "{\"nombre\": \"Don Mario\"}");
        String centro = crear("/api/franquicias/" + franquicia + "/sucursales", "{\"nombre\": \"Centro\"}");
        String norte = crear("/api/franquicias/" + franquicia + "/sucursales", "{\"nombre\": \"Norte\"}");
        crear("/api/franquicias/" + franquicia + "/sucursales", "{\"nombre\": \"Sin productos\"}");

        crear("/api/sucursales/" + centro + "/productos", "{\"nombre\": \"Pizza\", \"stock\": 30}");
        String gaseosa = crear("/api/sucursales/" + centro + "/productos", "{\"nombre\": \"Gaseosa\", \"stock\": 5}");
        crear("/api/sucursales/" + norte + "/productos", "{\"nombre\": \"Postre\", \"stock\": 8}");
        String cafe = crear("/api/sucursales/" + norte + "/productos", "{\"nombre\": \"Cafe\", \"stock\": 12}");

        // La gaseosa pasa a tener mas stock que la pizza.
        patch("/api/productos/" + gaseosa, "{\"stock\": 99}")
                .expectStatus().isOk()
                .expectBody().jsonPath("$.stock").isEqualTo(99);

        // Al eliminar el cafe, el postre pasa a ser el de mas stock en Norte.
        webTestClient.delete().uri("/api/productos/" + cafe)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri("/api/franquicias/" + franquicia + "/productos/mayor-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].nombre").isEqualTo("Gaseosa")
                .jsonPath("$[0].stock").isEqualTo(99)
                .jsonPath("$[0].sucursalNombre").isEqualTo("Centro")
                .jsonPath("$[1].nombre").isEqualTo("Postre")
                .jsonPath("$[1].sucursalNombre").isEqualTo("Norte");
    }

    @Test
    void renombrarFranquiciaSucursalYProducto() {
        String franquicia = crear("/api/franquicias", "{\"nombre\": \"Don Mario\"}");
        String sucursal = crear("/api/franquicias/" + franquicia + "/sucursales", "{\"nombre\": \"Centro\"}");
        String producto = crear("/api/sucursales/" + sucursal + "/productos", "{\"nombre\": \"Pizza\", \"stock\": 10}");

        patch("/api/franquicias/" + franquicia, "{\"nombre\": \"Don Mario Express\"}")
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nombre").isEqualTo("Don Mario Express");

        patch("/api/sucursales/" + sucursal, "{\"nombre\": \"Sede Centro\"}")
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nombre").isEqualTo("Sede Centro");

        patch("/api/productos/" + producto, "{\"nombre\": \"Pizza hawaiana\"}")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombre").isEqualTo("Pizza hawaiana")
                .jsonPath("$.stock").isEqualTo(10);
    }

    @Test
    void crearEnUnaFranquiciaQueNoExisteDevuelve404() {
        webTestClient.post().uri("/api/franquicias/no-existe/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\": \"Centro\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo(404)
                .jsonPath("$.mensaje").isEqualTo("No existe la franquicia con id no-existe");
    }

    @Test
    void unStockNegativoDevuelve400YNoModificaElProducto() {
        String franquicia = crear("/api/franquicias", "{\"nombre\": \"Don Mario\"}");
        String sucursal = crear("/api/franquicias/" + franquicia + "/sucursales", "{\"nombre\": \"Centro\"}");
        String producto = crear("/api/sucursales/" + sucursal + "/productos", "{\"nombre\": \"Pizza\", \"stock\": 10}");

        patch("/api/productos/" + producto, "{\"stock\": -5}")
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$.codigo").isEqualTo(400);

        webTestClient.get().uri("/api/franquicias/" + franquicia + "/productos/mayor-stock")
                .exchange()
                .expectBody().jsonPath("$[0].stock").isEqualTo(10);
    }

    private String crear(String uri, String json) {
        Map<?, ?> respuesta = webTestClient.post().uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED)
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        return (String) respuesta.get("id");
    }

    private WebTestClient.ResponseSpec patch(String uri, String json) {
        return webTestClient.patch().uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange();
    }
}
