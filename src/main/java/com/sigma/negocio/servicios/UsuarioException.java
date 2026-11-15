package com.sigma.negocio.servicios;

public final class UsuarioException extends Exception {
    public enum Motivo {
        CREDENCIALES_INVALIDAS,
        SIN_PERMISO,
        DATOS_INVALIDOS,
        USERNAME_DUPLICADO,
        USUARIO_NO_ENCONTRADO,
        ADMIN_YA_EXISTE,
        ERROR_PERSISTENCIA
    }

    private final Motivo motivo;

    UsuarioException(Motivo motivo, String mensaje) {
        super(mensaje);
        this.motivo = motivo;
    }

    public Motivo getMotivo() {
        return motivo;
    }
}
