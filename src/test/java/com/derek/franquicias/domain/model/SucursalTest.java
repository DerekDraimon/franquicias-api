package com.derek.franquicias.domain.model;

import com.derek.franquicias.domain.exception.DatoInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SucursalTest {

    @Test
    void noPermiteNombreVacio() {
        assertThrows(DatoInvalidoException.class, () -> new Sucursal(null, "", "f1"));
    }

    @Test
    void conNombreMantieneLaFranquicia() {
        Sucursal original = new Sucursal("s1", "Centro", "f1");

        Sucursal actualizada = original.conNombre("Sede Centro");

        assertEquals("Sede Centro", actualizada.nombre());
        assertEquals("f1", actualizada.franquiciaId());
    }
}
