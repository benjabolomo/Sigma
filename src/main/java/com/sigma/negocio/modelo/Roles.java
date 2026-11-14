package com.sigma.negocio.modelo;

public enum Roles {
    ADMINISTRADOR,
    MECANICO,
    RECEPCIONISTA;

    public boolean tienePermiso(Permiso permiso) {
        if (permiso == null) {
            return false;
        }
        return switch (permiso) {
            case CONSULTAR_CLIENTES, CONSULTAR_VEHICULOS, GESTIONAR_SERVICIOS -> true;
            case GESTIONAR_CLIENTES, GESTIONAR_VEHICULOS -> this != MECANICO;
            case GESTIONAR_USUARIOS -> this == ADMINISTRADOR;
        };
    }
}
