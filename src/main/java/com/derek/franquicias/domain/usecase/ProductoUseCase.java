package com.derek.franquicias.domain.usecase;

import com.derek.franquicias.domain.exception.DatoInvalidoException;
import com.derek.franquicias.domain.exception.RecursoNoEncontradoException;
import com.derek.franquicias.domain.gateway.ProductoGateway;
import com.derek.franquicias.domain.gateway.SucursalGateway;
import com.derek.franquicias.domain.model.Producto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ProductoUseCase {

    private final ProductoGateway productoGateway;
    private final SucursalGateway sucursalGateway;

    public ProductoUseCase(ProductoGateway productoGateway, SucursalGateway sucursalGateway) {
        this.productoGateway = productoGateway;
        this.sucursalGateway = sucursalGateway;
    }

    public Mono<Producto> crear(String sucursalId, String nombre, int stock) {
        return sucursalGateway.buscarPorId(sucursalId)
                .switchIfEmpty(Mono.error(
                        new RecursoNoEncontradoException("No existe la sucursal con id " + sucursalId)))
                .flatMap(sucursal -> productoGateway.guardar(new Producto(null, nombre, stock, sucursal.id())));
    }

    public Flux<Producto> listarPorSucursal(String sucursalId) {
        return sucursalGateway.buscarPorId(sucursalId)
                .switchIfEmpty(Mono.error(
                        new RecursoNoEncontradoException("No existe la sucursal con id " + sucursalId)))
                .flatMapMany(sucursal -> productoGateway.buscarPorSucursal(sucursal.id()));
    }

    // nuevoNombre y nuevoStock pueden venir en null: solo se cambia lo que llega.
    public Mono<Producto> modificar(String id, String nuevoNombre, Integer nuevoStock) {
        if (nuevoNombre == null && nuevoStock == null) {
            return Mono.error(new DatoInvalidoException("Debe enviar el nombre o el stock a modificar"));
        }

        return buscarProducto(id)
                .map(producto -> {
                    Producto actualizado = producto;
                    if (nuevoNombre != null) {
                        actualizado = actualizado.conNombre(nuevoNombre);
                    }
                    if (nuevoStock != null) {
                        actualizado = actualizado.conStock(nuevoStock);
                    }
                    return actualizado;
                })
                .flatMap(productoGateway::guardar);
    }

    public Mono<Void> eliminar(String id) {
        return buscarProducto(id)
                .flatMap(producto -> productoGateway.eliminar(producto.id()));
    }

    private Mono<Producto> buscarProducto(String id) {
        return productoGateway.buscarPorId(id)
                .switchIfEmpty(Mono.error(
                        new RecursoNoEncontradoException("No existe el producto con id " + id)));
    }
}
