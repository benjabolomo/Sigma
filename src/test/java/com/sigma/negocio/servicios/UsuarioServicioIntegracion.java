package com.sigma.negocio.servicios;

import com.sigma.negocio.modelo.Roles;
import com.sigma.negocio.modelo.Permiso;
import static com.sigma.negocio.servicios.UsuarioException.Motivo.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

public final class UsuarioServicioIntegracion {
    public static void main(String[] args) throws Exception {
        String url = args[0];
        String usuarioDb = args[1];
        String claveDb = System.getenv("SIGMA_TEST_DB_PASSWORD");
        UsuarioServicio.ProveedorConexion abrir = () -> DriverManager.getConnection(url, usuarioDb, claveDb);
        UsuarioServicio servicio = new UsuarioServicio(abrir);
        char[] clave = "Una frase para la prueba 1".toCharArray();
        char[] nueva = "Otra frase para la prueba 2".toCharArray();

        try (Connection conexion = abrir.abrir(); var sql = conexion.createStatement();
                var fila = sql.executeQuery("SELECT 1 FROM usuario LIMIT 1")) {
            exigir(!fila.next());
        }

        CountDownLatch inicio = new CountDownLatch(1);
        try (var ejecutor = Executors.newFixedThreadPool(2)) {
            var primera = ejecutor.submit(() -> crearAlIniciar(inicio, servicio, clave));
            var segunda = ejecutor.submit(() -> crearAlIniciar(inicio, servicio, clave));
            inicio.countDown();
            exigir(primera.get() != segunda.get());
        }
        esperar(ADMIN_YA_EXISTE, () -> servicio.crearAdministradorInicial("Otra", "otra", clave));
        var admin = servicio.iniciarSesion("ana", clave);
        exigir(admin.getUsuario().getNombre().equals("Ana"));
        var mecanico = servicio.crearUsuario(admin, "Leo", null, "leo", clave, Roles.MECANICO);
        var recepcion = servicio.crearUsuario(admin, "Sol", "1234", "sol", clave, Roles.RECEPCIONISTA);
        esperar(ADMIN_YA_EXISTE, () -> servicio.crearUsuario(admin, "Luz", null, "luz", clave, Roles.ADMINISTRADOR));
        exigir(recepcion.getContacto().equals("1234"));
        exigir(servicio.listarUsuarios(admin).size() == 3);
        exigir(new String(clave).equals(claveGuardada(abrir, "ana")));
        exigir(new String(clave).equals(claveGuardada(abrir, "leo")));
        esperar(USERNAME_DUPLICADO, () -> servicio.crearUsuario(admin, "Duplicado", null, "leo", clave, Roles.MECANICO));
        esperar(DATOS_INVALIDOS, () -> servicio.crearUsuario(admin, "", null, "vacío", clave, Roles.MECANICO));
        esperar(DATOS_INVALIDOS, () -> servicio.crearUsuario(admin, "Uno", null, "a".repeat(51), clave, Roles.MECANICO));
        esperar(DATOS_INVALIDOS, () -> servicio.crearUsuario(admin, "Uno", null, "corto", new char[1], Roles.MECANICO));
        esperar(CREDENCIALES_INVALIDAS, () -> servicio.iniciarSesion("ana", nueva));
        esperar(CREDENCIALES_INVALIDAS, () -> servicio.iniciarSesion("nadie", clave));
        esperar(ERROR_PERSISTENCIA, () -> new UsuarioServicio(() -> {
            throw new SQLException("Conexión de prueba interrumpida");
        }).iniciarSesion("ana", clave));
        var sesionMecanico = servicio.iniciarSesion("leo", clave);
        var sesionRecepcion = servicio.iniciarSesion("sol", clave);
        for (var sesion : new UsuarioServicio.Sesion[] {admin, sesionMecanico, sesionRecepcion}) {
            for (Permiso permiso : Permiso.values()) {
                boolean permitido = sesion.getUsuario().getRol().tienePermiso(permiso);
                exigir(servicio.tienePermiso(sesion, permiso) == permitido);
                if (permitido) {
                    servicio.verificarPermiso(sesion, permiso);
                } else {
                    esperar(SIN_PERMISO, () -> servicio.verificarPermiso(sesion, permiso));
                }
            }
        }
        exigir(!servicio.tienePermiso(admin, null));
        esperar(SIN_PERMISO, () -> servicio.verificarPermiso(admin, null));
        esperar(SIN_PERMISO, () -> servicio.crearUsuario(sesionMecanico, "Otro", null, "otro", clave, Roles.MECANICO));
        esperar(SIN_PERMISO, () -> servicio.desactivarUsuario(sesionRecepcion, mecanico.getIdUsuario()));
        esperar(SIN_PERMISO, () -> servicio.listarUsuarios(null));
        esperar(USUARIO_NO_ENCONTRADO, () -> servicio.desactivarUsuario(admin, Long.MAX_VALUE));
        UsuarioServicio sinConexion = new UsuarioServicio(() -> {
            throw new SQLException("Conexión de prueba interrumpida");
        });
        esperar(ERROR_PERSISTENCIA, () -> sinConexion.tienePermiso(admin, Permiso.GESTIONAR_SERVICIOS));
        esperar(ERROR_PERSISTENCIA, () -> sinConexion.verificarPermiso(admin, Permiso.GESTIONAR_SERVICIOS));
        esperar(SIN_PERMISO, () -> servicio.listarUsuarios(sesionMecanico));
        esperar(SIN_PERMISO, () -> servicio.desactivarUsuario(admin, admin.getUsuario().getIdUsuario()));

        try (Connection conexion = abrir.abrir(); var sql = conexion.createStatement()) {
            sql.executeUpdate("INSERT INTO cliente (id_cliente, nombre) VALUES (1, 'De prueba')");
            sql.executeUpdate("INSERT INTO vehiculo (patente, modelo, id_cliente, id_tipo) VALUES ('TEST01', 'Auto', 1, 1)");
            sql.executeUpdate("INSERT INTO servicio (patente_vehiculo, id_usuario) VALUES ('TEST01', " + mecanico.getIdUsuario() + ")");
        }
        String claveMecanico = claveGuardada(abrir, "leo");
        esperar(USUARIO_NO_ENCONTRADO, () -> servicio.restablecerAdministrador("leo", nueva));
        exigir(claveMecanico.equals(claveGuardada(abrir, "leo")));
        servicio.desactivarUsuario(admin, mecanico.getIdUsuario());
        servicio.desactivarUsuario(admin, mecanico.getIdUsuario());
        for (Permiso permiso : Permiso.values()) {
            exigir(!servicio.tienePermiso(sesionMecanico, permiso));
            esperar(SIN_PERMISO, () -> servicio.verificarPermiso(sesionMecanico, permiso));
        }
        esperar(CREDENCIALES_INVALIDAS, () -> servicio.iniciarSesion("leo", clave));
        try (Connection conexion = abrir.abrir(); var sql = conexion.prepareStatement(
                "SELECT 1 FROM servicio s JOIN usuario u ON s.id_usuario = u.id_usuario WHERE u.id_usuario = ? AND NOT u.activo")) {
            sql.setLong(1, mecanico.getIdUsuario());
            try (var fila = sql.executeQuery()) {
                exigir(fila.next());
            }
        }

        String claveAdmin = claveGuardada(abrir, "ana");
        servicio.restablecerAdministrador("ana", nueva);
        exigir(!claveAdmin.equals(claveGuardada(abrir, "ana")));
        exigir(new String(nueva).equals(claveGuardada(abrir, "ana")));
        esperar(CREDENCIALES_INVALIDAS, () -> servicio.iniciarSesion("ana", clave));
        exigir(servicio.iniciarSesion("ana", nueva).getUsuario().getIdUsuario() == admin.getUsuario().getIdUsuario());
        exigir(servicio.listarUsuarios(admin).size() == 3);
        esperar(USUARIO_NO_ENCONTRADO, () -> servicio.restablecerAdministrador("nadie", nueva));

        // Simula datos heredados y cambios externos para verificar que no se confía en la sesión.
        try (Connection conexion = abrir.abrir(); var sql = conexion.createStatement()) {
            sql.executeUpdate("UPDATE usuario SET id_rol = (SELECT id_rol FROM roles WHERE nombre_rol = 'ADMINISTRADOR') WHERE username = 'sol'");
        }
        exigir(servicio.tienePermiso(sesionRecepcion, Permiso.GESTIONAR_USUARIOS));
        esperar(SIN_PERMISO, () -> servicio.desactivarUsuario(admin, recepcion.getIdUsuario()));
        try (Connection conexion = abrir.abrir(); var sql = conexion.createStatement()) {
            sql.executeUpdate("UPDATE usuario SET id_rol = (SELECT id_rol FROM roles WHERE nombre_rol = 'MECANICO') WHERE username = 'sol'");
        }
        exigir(!servicio.tienePermiso(sesionRecepcion, Permiso.GESTIONAR_CLIENTES));
        esperar(SIN_PERMISO, () -> servicio.listarUsuarios(sesionRecepcion));
        try (Connection conexion = abrir.abrir(); var sql = conexion.createStatement()) {
            sql.executeUpdate("DELETE FROM usuario WHERE username = 'sol'");
            sql.executeUpdate("UPDATE usuario SET activo = FALSE WHERE username = 'ana'");
        }
        exigir(!servicio.tienePermiso(sesionRecepcion, Permiso.CONSULTAR_CLIENTES));
        esperar(SIN_PERMISO, () -> servicio.verificarPermiso(sesionRecepcion, Permiso.CONSULTAR_CLIENTES));
        exigir(!servicio.tienePermiso(admin, Permiso.GESTIONAR_USUARIOS));
        esperar(SIN_PERMISO, () -> servicio.listarUsuarios(admin));
        esperar(ADMIN_YA_EXISTE, () -> servicio.crearAdministradorInicial("Otra", "otra", clave));
        String claveInactivo = claveGuardada(abrir, "ana");
        esperar(USUARIO_NO_ENCONTRADO, () -> servicio.restablecerAdministrador("ana", clave));
        exigir(claveInactivo.equals(claveGuardada(abrir, "ana")));
        System.out.println("Integración de usuarios verificada.");
    }

    private static String claveGuardada(UsuarioServicio.ProveedorConexion abrir, String username) throws SQLException {
        try (var conexion = abrir.abrir(); var sql = conexion.prepareStatement(
                "SELECT password_hash FROM usuario WHERE username = ?")) {
            sql.setString(1, username);
            try (var fila = sql.executeQuery()) {
                exigir(fila.next());
                return fila.getString(1);
            }
        }
    }

    private static boolean crearAlIniciar(CountDownLatch inicio, UsuarioServicio servicio,
            char[] clave) throws InterruptedException {
        inicio.await();
        try {
            servicio.crearAdministradorInicial("Ana", "ana", clave);
            return true;
        } catch (UsuarioException e) {
            exigir(e.getMotivo() == ADMIN_YA_EXISTE);
            return false;
        }
    }

    private static void esperar(UsuarioException.Motivo esperado, Operacion operacion) throws Exception {
        try {
            operacion.ejecutar();
        } catch (UsuarioException e) {
            if (e.getMotivo() != esperado) {
                throw new AssertionError("Se esperaba " + esperado + " y se obtuvo " + e.getMotivo(), e);
            }
            return;
        }
        throw new AssertionError("Se esperaba " + esperado);
    }

    private static void exigir(boolean resultado) {
        if (!resultado) {
            throw new AssertionError();
        }
    }

    @FunctionalInterface
    private interface Operacion {
        void ejecutar() throws Exception;
    }
}
