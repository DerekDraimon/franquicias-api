package com.derek.franquicias.config;

import com.derek.franquicias.domain.gateway.FranquiciaGateway;
import com.derek.franquicias.domain.gateway.ProductoGateway;
import com.derek.franquicias.domain.gateway.SucursalGateway;
import com.derek.franquicias.domain.usecase.FranquiciaUseCase;
import com.derek.franquicias.domain.usecase.MayorStockUseCase;
import com.derek.franquicias.domain.usecase.ProductoUseCase;
import com.derek.franquicias.domain.usecase.SucursalUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Los casos de uso no tienen anotaciones de Spring; aqui se registran como beans
// para que el dominio siga sin depender del framework.
@Configuration
public class UseCaseConfig {

    @Bean
    public FranquiciaUseCase franquiciaUseCase(FranquiciaGateway franquiciaGateway) {
        return new FranquiciaUseCase(franquiciaGateway);
    }

    @Bean
    public SucursalUseCase sucursalUseCase(SucursalGateway sucursalGateway, FranquiciaGateway franquiciaGateway) {
        return new SucursalUseCase(sucursalGateway, franquiciaGateway);
    }

    @Bean
    public ProductoUseCase productoUseCase(ProductoGateway productoGateway, SucursalGateway sucursalGateway) {
        return new ProductoUseCase(productoGateway, sucursalGateway);
    }

    @Bean
    public MayorStockUseCase mayorStockUseCase(FranquiciaGateway franquiciaGateway,
                                               SucursalGateway sucursalGateway,
                                               ProductoGateway productoGateway) {
        return new MayorStockUseCase(franquiciaGateway, sucursalGateway, productoGateway);
    }
}
