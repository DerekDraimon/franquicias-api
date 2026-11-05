package com.derek.franquicias.infrastructure.mongo.sucursal;

import com.derek.franquicias.domain.model.Sucursal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataMongoTest
@Testcontainers
@Import(SucursalMongoAdapter.class)
class SucursalMongoAdapterTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8");

    @Autowired
    private SucursalMongoAdapter adapter;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Test
    void guardarAsignaUnIdYLuegoSePuedeBuscar() {
        Sucursal guardada = adapter.guardar(new Sucursal(null, "Centro", "f1")).block();

        assertNotNull(guardada.id());
        StepVerifier.create(adapter.buscarPorId(guardada.id()))
                .expectNext(new Sucursal(guardada.id(), "Centro", "f1"))
                .verifyComplete();
    }

    @Test
    void buscarPorFranquiciaDevuelveSoloLasSucursalesDeEsaFranquicia() {
        String franquiciaId = UUID.randomUUID().toString();
        adapter.guardar(new Sucursal(null, "Centro", franquiciaId)).block();
        adapter.guardar(new Sucursal(null, "Norte", franquiciaId)).block();
        adapter.guardar(new Sucursal(null, "De otra franquicia", "otra")).block();

        StepVerifier.create(adapter.buscarPorFranquicia(franquiciaId).map(Sucursal::nombre).sort())
                .expectNext("Centro", "Norte")
                .verifyComplete();
    }

    @Test
    void creaElIndicePorFranquicia() {
        adapter.guardar(new Sucursal(null, "Centro", "f1")).block();

        List<IndexInfo> indices = mongoTemplate.indexOps(SucursalDocument.class)
                .getIndexInfo().collectList().block();

        assertTrue(indices.stream().anyMatch(indice -> indice.isIndexForFields(List.of("franquiciaId"))));
    }
}
