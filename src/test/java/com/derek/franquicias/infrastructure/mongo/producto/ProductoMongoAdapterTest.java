package com.derek.franquicias.infrastructure.mongo.producto;

import com.derek.franquicias.domain.model.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@Testcontainers
@Import(ProductoMongoAdapter.class)
class ProductoMongoAdapterTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8");

    @Autowired
    private ProductoMongoAdapter adapter;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    private String nuevaSucursal() {
        return UUID.randomUUID().toString();
    }

    @Test
    void guardarAsignaUnIdYLuegoSePuedeBuscar() {
        Producto guardado = adapter.guardar(new Producto(null, "Pizza", 10, "s1")).block();

        assertNotNull(guardado.id());
        StepVerifier.create(adapter.buscarPorId(guardado.id()))
                .expectNext(new Producto(guardado.id(), "Pizza", 10, "s1"))
                .verifyComplete();
    }

    @Test
    void eliminarBorraElProducto() {
        Producto guardado = adapter.guardar(new Producto(null, "Pizza", 10, "s1")).block();

        adapter.eliminar(guardado.id()).block();

        StepVerifier.create(adapter.buscarPorId(guardado.id()))
                .verifyComplete();
    }

    @Test
    void buscarMayorStockDevuelveElProductoConMasStockDeLaSucursal() {
        String sucursalId = nuevaSucursal();
        adapter.guardar(new Producto(null, "Pizza", 10, sucursalId)).block();
        adapter.guardar(new Producto(null, "Gaseosa", 80, sucursalId)).block();
        adapter.guardar(new Producto(null, "Postre", 25, sucursalId)).block();
        adapter.guardar(new Producto(null, "De otra sucursal", 500, nuevaSucursal())).block();

        StepVerifier.create(adapter.buscarMayorStockDeSucursal(sucursalId).map(Producto::nombre))
                .expectNext("Gaseosa")
                .verifyComplete();
    }

    @Test
    void buscarPorSucursalDevuelveSoloLosProductosDeEsaSucursal() {
        String sucursalId = nuevaSucursal();
        adapter.guardar(new Producto(null, "Pizza", 10, sucursalId)).block();
        adapter.guardar(new Producto(null, "Gaseosa", 5, sucursalId)).block();
        adapter.guardar(new Producto(null, "De otra sucursal", 1, nuevaSucursal())).block();

        StepVerifier.create(adapter.buscarPorSucursal(sucursalId).map(Producto::nombre).sort())
                .expectNext("Gaseosa", "Pizza")
                .verifyComplete();
    }

    @Test
    void buscarMayorStockEnUnaSucursalSinProductosDevuelveVacio() {
        StepVerifier.create(adapter.buscarMayorStockDeSucursal(nuevaSucursal()))
                .verifyComplete();
    }

    @Test
    void creaElIndicePorSucursalYStock() {
        adapter.guardar(new Producto(null, "Pizza", 10, "s1")).block();

        // Spring Data crea los indices de forma asincrona: se reintenta hasta que aparezca.
        StepVerifier.create(mongoTemplate.indexOps(ProductoDocument.class).getIndexInfo()
                        .filter(indice -> indice.isIndexForFields(List.of("sucursalId", "stock")))
                        .next()
                        .repeatWhenEmpty(intentos -> intentos.take(20).delayElements(Duration.ofMillis(250))))
                .expectNextCount(1)
                .verifyComplete();
    }
}
