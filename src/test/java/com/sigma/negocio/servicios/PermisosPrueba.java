package com.sigma.negocio.servicios;

import com.sigma.negocio.modelo.Permiso;
import com.sigma.negocio.modelo.Roles;
import java.util.Set;

public final class PermisosPrueba {
    public static void main(String[] args) throws Exception {
        comprobar(Roles.ADMINISTRADOR, Set.of(Permiso.values()));
        comprobar(Roles.RECEPCIONISTA, Set.of(Permiso.CONSULTAR_CLIENTES, Permiso.GESTIONAR_CLIENTES,
                Permiso.CONSULTAR_VEHICULOS, Permiso.GESTIONAR_VEHICULOS, Permiso.GESTIONAR_SERVICIOS));
        comprobar(Roles.MECANICO, Set.of(Permiso.CONSULTAR_CLIENTES, Permiso.CONSULTAR_VEHICULOS,
                Permiso.GESTIONAR_SERVICIOS));
        UsuarioServicio servicio = new UsuarioServicio(() -> {
            throw new AssertionError("Una sesión ausente no debe consultar la base.");
        });
        for (Permiso permiso : Permiso.values()) {
            if (servicio.tienePermiso(null, permiso)) {
                throw new AssertionError("Sesión ausente autorizada.");
            }
            try {
                servicio.verificarPermiso(null, permiso);
                throw new AssertionError("Se esperaba SIN_PERMISO.");
            } catch (UsuarioException e) {
                if (e.getMotivo() != UsuarioException.Motivo.SIN_PERMISO) {
                    throw new AssertionError(e);
                }
            }
        }
        System.out.println("Matriz de permisos verificada.");
    }

    private static void comprobar(Roles rol, Set<Permiso> esperados) {
        for (Permiso permiso : Permiso.values()) {
            if (rol.tienePermiso(permiso) != esperados.contains(permiso)) {
                throw new AssertionError(rol + ": " + permiso);
            }
        }
        if (rol.tienePermiso(null)) {
            throw new AssertionError("Permiso nulo autorizado.");
        }
    }
}
