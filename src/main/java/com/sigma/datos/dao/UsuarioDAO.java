package com.sigma.datos.dao;

import com.sigma.negocio.modelo.Roles;
import com.sigma.negocio.modelo.Usuarios;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class UsuarioDAO {
    private static final String PERFIL = """
            SELECT u.id_usuario, u.nombre, u.contacto, u.username, r.nombre_rol, u.activo
            """;
    private static final String DESDE_USUARIO = """
             FROM usuario u JOIN roles r ON u.id_rol = r.id_rol
            """;

    public static final class Credenciales {
        private final Usuarios usuario;
        private final String clave;

        private Credenciales(Usuarios usuario, String clave) {
            this.usuario = usuario;
            this.clave = clave;
        }

        public Usuarios getUsuario() {
            return usuario;
        }

        public String getClave() {
            return clave;
        }
    }

    public Credenciales buscarCredenciales(Connection conexion, String username) throws SQLException {
        String sql = PERFIL + ", u.password_hash" + DESDE_USUARIO + " WHERE u.username = ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, username);
            try (ResultSet fila = consulta.executeQuery()) {
                return fila.next() ? new Credenciales(leerUsuario(fila), fila.getString("password_hash")) : null;
            }
        }
    }

    public List<Usuarios> listar(Connection conexion) throws SQLException {
        String sql = PERFIL + DESDE_USUARIO + " ORDER BY u.username";
        List<Usuarios> usuarios = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement(sql);
                ResultSet filas = consulta.executeQuery()) {
            while (filas.next()) {
                usuarios.add(leerUsuario(filas));
            }
        }
        return List.copyOf(usuarios);
    }

    public int bloquearRolAdministrador(Connection conexion) throws SQLException {
        return buscarIdRol(conexion, Roles.ADMINISTRADOR, " FOR UPDATE");
    }

    public boolean existeAdministrador(Connection conexion, int idRol) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(
                "SELECT 1 FROM usuario WHERE id_rol = ? LIMIT 1")) {
            consulta.setInt(1, idRol);
            try (ResultSet fila = consulta.executeQuery()) {
                return fila.next();
            }
        }
    }

    public Usuarios buscarPorId(Connection conexion, long idUsuario) throws SQLException {
        String sql = PERFIL + DESDE_USUARIO + " WHERE u.id_usuario = ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setLong(1, idUsuario);
            try (ResultSet fila = consulta.executeQuery()) {
                return fila.next() ? leerUsuario(fila) : null;
            }
        }
    }

    public Usuarios insertar(Connection conexion, String nombre, String contacto, String username,
            String clave, Roles rol) throws SQLException {
        int idRol = buscarIdRol(conexion, rol, "");
        String sql = "INSERT INTO usuario (nombre, contacto, username, password_hash, id_rol) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, nombre);
            consulta.setString(2, contacto);
            consulta.setString(3, username);
            consulta.setString(4, clave);
            consulta.setInt(5, idRol);
            consulta.executeUpdate();
            try (ResultSet claves = consulta.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("MySQL no devolvió el ID del usuario.");
                }
                return new Usuarios(claves.getLong(1), nombre, contacto, username, rol, true);
            }
        }
    }

    public boolean cambiarEstado(Connection conexion, long idUsuario, boolean activo)
        throws SQLException {
    String sql = "UPDATE usuario SET activo = ? WHERE id_usuario = ? AND activo <> ?";
    try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
        consulta.setBoolean(1, activo);
        consulta.setLong(2, idUsuario);
        consulta.setBoolean(3, activo);

        if (consulta.executeUpdate() == 1) {
            return true;
        }
    }

    try (PreparedStatement consulta = conexion.prepareStatement(
            "SELECT 1 FROM usuario WHERE id_usuario = ?")) {
        consulta.setLong(1, idUsuario);
        try (ResultSet fila = consulta.executeQuery()) {
            return fila.next();
        }
    }
}

    public boolean restablecerAdministrador(Connection conexion, String username, int idRol, String clave)
            throws SQLException {
        String sql = "UPDATE usuario SET password_hash = ? WHERE username = ? AND id_rol = ? AND activo = TRUE";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, clave);
            consulta.setString(2, username);
            consulta.setInt(3, idRol);
            return consulta.executeUpdate() == 1;
        }
    }

    private int buscarIdRol(Connection conexion, Roles rol, String bloqueo) throws SQLException {
        try (PreparedStatement consulta = conexion.prepareStatement(
                "SELECT id_rol FROM roles WHERE nombre_rol = ?" + bloqueo)) {
            consulta.setString(1, rol.name());
            try (ResultSet fila = consulta.executeQuery()) {
                if (!fila.next()) {
                    throw new SQLException("Falta un rol obligatorio en el catálogo.");
                }
                return fila.getInt("id_rol");
            }
        }
    }

    private Usuarios leerUsuario(ResultSet fila) throws SQLException {
        try {
            return new Usuarios(fila.getLong("id_usuario"), fila.getString("nombre"), fila.getString("contacto"),
                    fila.getString("username"), Roles.valueOf(fila.getString("nombre_rol")), fila.getBoolean("activo"));
        } catch (IllegalArgumentException e) {
            throw new SQLException("El usuario tiene un rol fuera del catálogo de la aplicación.", e);
        }
    }
}
