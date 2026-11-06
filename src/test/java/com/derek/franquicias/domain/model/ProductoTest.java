package com.derek.franquicias.domain.model;

import com.derek.franquicias.domain.exception.DatoInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductoTest {

    @Test
    void noPermiteStockNegativo() {
        assertThrows(DatoInvalidoException.class,
                () -> new Producto(null, "Pizza", -1, "s1"));
    }

    @Test
    void noPermiteNombreVacio() {
        assertThrows(DatoInvalidoException.class,
                () -> new Producto(null, "  ", 10, "s1"));
    }

    @Test
    void conStockDevuelveUnProductoNuevoSinCambiarElOriginal() {
        Producto original = new Producto("p1", "Pizza", 10, "s1");

        Producto actualizado = original.conStock(50);

        assertEquals(50, actualizado.stock());
        assertEquals(10, original.stock());
        assertEquals("Pizza", actualizado.nombre());
    }

    @Test
    void conNombreCambiaSoloElNombre() {
        Producto original = new Producto("p1", "Pizza", 10, "s1");

        Producto actualizado = original.conNombre("Pizza hawaiana");

        assertEquals("Pizza hawaiana", actualizado.nombre());
        assertEquals(10, actualizado.stock());
    }
}
