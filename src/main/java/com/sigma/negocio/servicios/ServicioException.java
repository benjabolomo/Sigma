package com.sigma.negocio.servicios;

/**
 * Excepción de dominio para errores en las reglas de negocio del módulo de servicios.
 */
public class ServicioException extends Exception {

    public ServicioException(String message) {
        super(message);
    }

    public ServicioException(String message, Throwable cause) {
        super(message, cause);
    }
}
