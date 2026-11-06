package com.derek.franquicias.infrastructure.entrypoint.producto;

import com.derek.franquicias.domain.usecase.MayorStockUseCase;
import com.derek.franquicias.domain.usecase.ProductoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductoUseCase productoUseCase;
    private final MayorStockUseCase mayorStockUseCase;

    public ProductoController(ProductoUseCase productoUseCase, MayorStockUseCase mayorStockUseCase) {
        this.productoUseCase = productoUseCase;
        this.mayorStockUseCase = mayorStockUseCase;
    }

    @PostMapping("/sucursales/{sucursalId}/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductoResponse> crear(@PathVariable String sucursalId, @RequestBody CrearProductoRequest request) {
        return productoUseCase.crear(sucursalId, request.nombre(), request.stockOCero())
                .map(ProductoResponse::desde);
    }

    @GetMapping("/sucursales/{sucursalId}/productos")
    public Flux<ProductoResponse> listar(@PathVariable String sucursalId) {
        return productoUseCase.listarPorSucursal(sucursalId)
                .map(ProductoResponse::desde);
    }

    @PatchMapping("/productos/{id}")
    public Mono<ProductoResponse> modificar(@PathVariable String id, @RequestBody ModificarProductoRequest request) {
        return productoUseCase.modificar(id, request.nombre(), request.stock())
                .map(ProductoResponse::desde);
    }

    @DeleteMapping("/productos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminar(@PathVariable String id) {
        return productoUseCase.eliminar(id);
    }

    @GetMapping("/franquicias/{franquiciaId}/productos/mayor-stock")
    public Flux<ProductoMayorStockResponse> mayorStock(@PathVariable String franquiciaId) {
        return mayorStockUseCase.consultarPorFranquicia(franquiciaId)
                .map(ProductoMayorStockResponse::desde);
    }
}
