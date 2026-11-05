package com.derek.franquicias.domain.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, String id) {
        super("No existe la " + recurso + " con id " + id);
    }
}
