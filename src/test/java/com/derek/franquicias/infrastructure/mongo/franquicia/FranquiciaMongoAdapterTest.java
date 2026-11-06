package com.derek.franquicias.infrastructure.mongo.franquicia;

import com.derek.franquicias.domain.model.Franquicia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@Testcontainers
@Import(FranquiciaMongoAdapter.class)
class FranquiciaMongoAdapterTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8");

    @Autowired
    private FranquiciaMongoAdapter adapter;

    @Test
    void guardarAsignaUnIdYLuegoSePuedeBuscar() {
        Franquicia guardada = adapter.guardar(new Franquicia(null, "Don Mario")).block();

        assertNotNull(guardada.id());
        StepVerifier.create(adapter.buscarPorId(guardada.id()))
                .expectNext(new Franquicia(guardada.id(), "Don Mario"))
                .verifyComplete();
    }

    @Test
    void guardarConUnIdExistenteActualizaElNombre() {
        Franquicia guardada = adapter.guardar(new Franquicia(null, "Don Mario")).block();

        adapter.guardar(guardada.conNombre("Don Mario Express")).block();

        Franquicia leida = adapter.buscarPorId(guardada.id()).block();
        assertEquals("Don Mario Express", leida.nombre());
    }

    @Test
    void buscarUnIdQueNoExisteDevuelveVacio() {
        StepVerifier.create(adapter.buscarPorId("no-existe"))
                .verifyComplete();
    }
}
