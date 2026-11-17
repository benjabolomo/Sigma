package com.sigma.negocio.servicios;

import com.sigma.datos.conexion.Conexion;
import com.sigma.datos.dao.UsuarioDAO;
import com.sigma.negocio.modelo.Permiso;
import com.sigma.negocio.modelo.Roles;
import com.sigma.negocio.modelo.Usuarios;
import static com.sigma.negocio.servicios.UsuarioException.Motivo.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class UsuarioServicio {
    private static final int MAX_USERNAME = 50;
    private static final int MIN_CLAVE = 8;
    private static final int MAX_CLAVE = 128;
    private final UsuarioDAO dao = new UsuarioDAO();
    private final ProveedorConexion proveedorConexion;

    public UsuarioServicio() {
        this(Conexion::getConnection);
    }

    UsuarioServicio(ProveedorConexion proveedorConexion) {
        this.proveedorConexion = proveedorConexion;
    }

    public static final class Sesion {
        private final Usuarios usuario;

        private Sesion(Usuarios usuario) {
            this.usuario = usuario;
        }

        public Usuarios getUsuario() {
            return usuario;
        }
    }

    public Sesion iniciarSesion(String username, char[] clave) throws UsuarioException {
        String identificador = username == null ? "" : username.strip();
        if (identificador.isEmpty() || excedeLongitud(identificador, MAX_USERNAME)
                || clave == null || clave.length == 0 || clave.length > MAX_CLAVE * 2
                || Character.codePointCount(clave, 0, clave.length) > MAX_CLAVE) {
            throw credencialesInvalidas();
        }

        UsuarioDAO.Credenciales credenciales;
        try (Connection conexion = proveedorConexion.abrir()) {
            credenciales = dao.buscarCredenciales(conexion, identificador);
        } catch (SQLException e) {
            throw traducirError(e);
        }
        if (credenciales == null || !credenciales.getUsuario().isActivo()
                || !new String(clave).equals(credenciales.getClave())) {
            throw credencialesInvalidas();
        }
        return new Sesion(credenciales.getUsuario());
    }

    public Usuarios crearUsuario(Sesion sesion, String nombre, String contacto, String username, char[] clave,
            Roles rol) throws UsuarioException {
        return gestionarCuentas((conexion, idRolAdmin) -> {
            verificarPermiso(conexion, sesion, Permiso.GESTIONAR_USUARIOS);
            return insertarUsuario(conexion, idRolAdmin, nombre, contacto, username, clave, rol);
        });
    }

    public List<Usuarios> listarUsuarios(Sesion sesion) throws UsuarioException {
        return gestionarCuentas((conexion, idRolAdmin) -> {
            verificarPermiso(conexion, sesion, Permiso.GESTIONAR_USUARIOS);
            return dao.listar(conexion);
        });
    }

    public void desactivarUsuario(Sesion sesion, long idUsuario) throws UsuarioException {
        gestionarCuentas((conexion, idRolAdmin) -> {
            verificarPermiso(conexion, sesion, Permiso.GESTIONAR_USUARIOS);
            Usuarios destino = dao.buscarPorId(conexion, idUsuario);
            if (destino != null && destino.getRol() == Roles.ADMINISTRADOR) {
                throw new UsuarioException(SIN_PERMISO, "No se puede desactivar una cuenta administradora.");
            }
            if (!dao.desactivar(conexion, idUsuario)) {
                throw new UsuarioException(USUARIO_NO_ENCONTRADO, "El usuario indicado no existe.");
            }
            return null;
        });
    }

    void crearAdministradorInicial(String nombre, String username, char[] clave) throws UsuarioException {
        gestionarCuentas((conexion, idRolAdmin) -> {
            return insertarUsuario(conexion, idRolAdmin, nombre, null, username, clave, Roles.ADMINISTRADOR);
        });
    }

    void restablecerAdministrador(String username, char[] clave) throws UsuarioException {
        String identificador = validarTexto(username, "Usuario", MAX_USERNAME);
        String claveValidada = validarClaveNueva(clave);
        gestionarCuentas((conexion, idRolAdmin) -> {
            if (!dao.restablecerAdministrador(conexion, identificador, idRolAdmin, claveValidada)) {
                throw new UsuarioException(USUARIO_NO_ENCONTRADO, "No existe un administrador activo con ese usuario.");
            }
            return null;
        });
    }

    private Usuarios insertarUsuario(Connection conexion, int idRolAdmin, String nombre, String contacto, String username,
            char[] clave, Roles rol) throws SQLException, UsuarioException {
        if (rol == Roles.ADMINISTRADOR && dao.existeAdministrador(conexion, idRolAdmin)) {
            throw new UsuarioException(ADMIN_YA_EXISTE, "Ya existe una cuenta administradora.");
        }
        String nombreValidado = validarTexto(nombre, "Nombre", 100);
        String identificador = validarTexto(username, "Usuario", MAX_USERNAME);
        String contactoValidado = contacto == null || contacto.isBlank() ? null : validarTexto(contacto, "Contacto", 100);
        if (rol == null) {
            throw new UsuarioException(DATOS_INVALIDOS, "Seleccioná un rol.");
        }
        return dao.insertar(conexion, nombreValidado, contactoValidado, identificador, validarClaveNueva(clave), rol);
    }

    public boolean tienePermiso(Sesion sesion, Permiso permiso) throws UsuarioException {
        if (sesion == null || permiso == null) {
            return false;
        }
        try (Connection conexion = proveedorConexion.abrir()) {
            return tienePermiso(conexion, sesion, permiso);
        } catch (SQLException e) {
            throw traducirError(e);
        }
    }

    public void verificarPermiso(Sesion sesion, Permiso permiso) throws UsuarioException {
        if (!tienePermiso(sesion, permiso)) {
            throw new UsuarioException(SIN_PERMISO, "La sesión no tiene el permiso requerido.");
        }
    }

    private boolean tienePermiso(Connection conexion, Sesion sesion, Permiso permiso) throws SQLException {
        if (sesion == null || permiso == null) {
            return false;
        }
        Usuarios actual = dao.buscarPorId(conexion, sesion.getUsuario().getIdUsuario());
        return actual != null && actual.isActivo() && actual.getRol().tienePermiso(permiso);
    }

    private void verificarPermiso(Connection conexion, Sesion sesion, Permiso permiso)
            throws SQLException, UsuarioException {
        if (!tienePermiso(conexion, sesion, permiso)) {
            throw new UsuarioException(SIN_PERMISO, "La sesión no tiene el permiso requerido.");
        }
    }

    private <T> T gestionarCuentas(OperacionCuentas<T> operacion) throws UsuarioException {
        try (Connection conexion = proveedorConexion.abrir()) {
            conexion.setAutoCommit(false);
            try {
                int idRolAdmin = dao.bloquearRolAdministrador(conexion);
                T resultado = operacion.ejecutar(conexion, idRolAdmin);
                conexion.commit();
                return resultado;
            } catch (SQLException | UsuarioException | RuntimeException e) {
                try {
                    conexion.rollback();
                } catch (SQLException falloReversion) {
                    e.addSuppressed(falloReversion);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw traducirError(e);
        }
    }

    private static String validarTexto(String valor, String campo, int maximo) throws UsuarioException {
        String texto = valor == null ? "" : valor.strip();
        if (texto.isEmpty() || excedeLongitud(texto, maximo)) {
            throw new UsuarioException(DATOS_INVALIDOS, campo + " debe tener entre 1 y " + maximo + " caracteres.");
        }
        return texto;
    }

    private static boolean excedeLongitud(String texto, int maximo) {
        return texto.codePointCount(0, texto.length()) > maximo;
    }

    private static String validarClaveNueva(char[] clave) throws UsuarioException {
        int longitud = clave == null || clave.length > MAX_CLAVE * 2
                ? 0 : Character.codePointCount(clave, 0, clave.length);
        if (longitud < MIN_CLAVE || longitud > MAX_CLAVE) {
            throw new UsuarioException(DATOS_INVALIDOS, "La contraseña debe tener mas de 8 caracteres.");
        }
        return new String(clave);
    }

    private static UsuarioException credencialesInvalidas() {
        return new UsuarioException(CREDENCIALES_INVALIDAS, "Usuario o contraseña incorrectos.");
    }

    private static UsuarioException traducirError(SQLException error) {
        if (error.getErrorCode() == 1062) {
            return new UsuarioException(USERNAME_DUPLICADO, "Ese nombre de usuario ya está en uso.");
        }
        return new UsuarioException(ERROR_PERSISTENCIA, "No se pudo completar la operación en la base de datos.");
    }

    @FunctionalInterface
    private interface OperacionCuentas<T> {
        T ejecutar(Connection conexion, int idRolAdmin) throws SQLException, UsuarioException;
    }

    @FunctionalInterface
    interface ProveedorConexion {
        Connection abrir() throws SQLException;
    }
}
