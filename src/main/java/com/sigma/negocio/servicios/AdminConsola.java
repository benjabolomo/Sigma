package com.sigma.negocio.servicios;

import java.io.Console;
import java.util.Arrays;

public final class AdminConsola {
    private AdminConsola() {
    }

    public static void main(String[] args) {
        int resultado = ejecutar(args, System.console());
        if (resultado != 0) {
            System.exit(resultado);
        }
    }

    private static int ejecutar(String[] args, Console consola) {
        boolean crear = args.length == 1 && "crear-admin".equals(args[0]);
        boolean restablecer = args.length == 2 && "restablecer-admin".equals(args[0]);
        if (!crear && !restablecer) {
            System.err.println("Uso: AdminConsola crear-admin | restablecer-admin <username>");
            return 2;
        }
        if (consola == null || !consola.isTerminal()) {
            System.err.println("Ejecutá el comando en una terminal que permita ocultar la contraseña.");
            return 2;
        }

        String nombre = crear ? consola.readLine("Nombre: ") : null;
        String username = crear ? consola.readLine("Usuario: ") : args[1];
        if (username == null || (crear && nombre == null)) {
            System.err.println("Operación cancelada.");
            return 2;
        }
        char[] clave = consola.readPassword("Nueva contraseña: ");
        if (clave == null) {
            System.err.println("Operación cancelada.");
            return 2;
        }

        char[] confirmacion = null;
        try {
            confirmacion = consola.readPassword("Repetí la contraseña: ");
            if (!Arrays.equals(clave, confirmacion)) {
                System.err.println("Las contraseñas deben coincidir.");
                return 2;
            }
            UsuarioServicio servicio = new UsuarioServicio();
            if (crear) {
                servicio.crearAdministradorInicial(nombre, username, clave);
                consola.printf("Administrador creado.%n");
            } else {
                servicio.restablecerAdministrador(username, clave);
                consola.printf("Contraseña restablecida.%n");
            }
            return 0;
        } catch (UsuarioException e) {
            System.err.println(e.getMessage());
            return 1;
        } finally {
            Arrays.fill(clave, '\0');
            if (confirmacion != null) {
                Arrays.fill(confirmacion, '\0');
            }
        }
    }
}
