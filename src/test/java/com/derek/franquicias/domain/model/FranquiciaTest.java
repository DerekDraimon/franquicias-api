package com.derek.franquicias.domain.model;

import com.derek.franquicias.domain.exception.DatoInvalidoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FranquiciaTest {

    @Test
    void noPermiteNombreNulo() {
        assertThrows(DatoInvalidoException.class, () -> new Franquicia(null, null));
    }

    @Test
    void conNombreDevuelveUnaFranquiciaNueva() {
        Franquicia original = new Franquicia("f1", "Don Mario");

        Franquicia actualizada = original.conNombre("Don Mario Express");

        assertEquals("Don Mario Express", actualizada.nombre());
        assertEquals("Don Mario", original.nombre());
    }
}
