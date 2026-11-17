package com.sigma.presentacion.usuarios;

import com.sigma.negocio.modelo.Roles;
import com.sigma.negocio.modelo.Usuarios;
import com.sigma.negocio.servicios.UsuarioException;
import com.sigma.negocio.servicios.UsuarioServicio;
import com.sigma.presentacion.componentes.DialogoConfirmacion;
import com.sigma.presentacion.componentes.Estilos;
import com.sigma.presentacion.componentes.TablaPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UsuarioPanel extends JPanel {

    private final UsuarioServicio usuarioServicio;
    private final UsuarioServicio.Sesion sesion;

    private JTextField txtNombre;
    private JTextField txtContacto;
    private JTextField txtUsername;
    private JPasswordField txtClave;
    private JComboBox<Roles> cboRol;
    private TablaPanel tabla;

    public UsuarioPanel(UsuarioServicio.Sesion sesion) {
        this.usuarioServicio = new UsuarioServicio();
        this.sesion = sesion;
        inicializarComponentes();
        cargarTabla();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(Estilos.MARGEN, Estilos.MARGEN, Estilos.MARGEN, Estilos.MARGEN));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(Estilos.bordeSeccion("Nuevo Usuario del Sistema"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNombre = new JTextField(16);
        txtContacto = new JTextField(16);
        txtUsername = new JTextField(14);
        txtClave = new JPasswordField(16);
        cboRol = new JComboBox<>(new Roles[]{Roles.MECANICO, Roles.RECEPCIONISTA});

        gbc.gridx = 0; gbc.gridy = 0; pnlForm.add(new JLabel("Nombre (*):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; pnlForm.add(txtNombre, gbc);
        gbc.gridx = 2; gbc.weightx = 0; pnlForm.add(new JLabel("Contacto:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1; pnlForm.add(txtContacto, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; pnlForm.add(new JLabel("Usuario (*):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; pnlForm.add(txtUsername, gbc);
        gbc.gridx = 2; gbc.weightx = 0; pnlForm.add(new JLabel("Rol (*):"), gbc);
        gbc.gridx = 3; gbc.weightx = 1; pnlForm.add(cboRol, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; pnlForm.add(new JLabel("Contraseña (mín. 8) (*):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; pnlForm.add(txtClave, gbc);

        JButton btnCrear = Estilos.botonPrimario("Crear Usuario");
        btnCrear.addActionListener(e -> crearUsuario());
        gbc.gridx = 3; gbc.weightx = 0; pnlForm.add(btnCrear, gbc);

        add(pnlForm, BorderLayout.NORTH);

        tabla = new TablaPanel("Usuarios Registrados", "ID", "Nombre", "Usuario", "Contacto", "Rol", "Estado");
        tabla.setAnchos(50, 180, 120, 160, 120, 90);
        tabla.setColumnasCentradas(0, 4, 5);
        add(tabla, BorderLayout.CENTER);

        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JButton btnCambiarEstado = Estilos.botonSecundario("Cambiar estado");
        btnCambiarEstado.addActionListener(e -> cambiarEstadoSeleccionado());
        JButton btnRefrescar = Estilos.botonSecundario("Refrescar");
        btnRefrescar.addActionListener(e -> cargarTabla());
        pnlAcciones.add(btnCambiarEstado);
        pnlAcciones.add(btnRefrescar);
        add(pnlAcciones, BorderLayout.SOUTH);
    }

    private void cargarTabla() {
        try {
            List<Usuarios> lista = usuarioServicio.listarUsuarios(sesion);
            List<Object[]> filas = new ArrayList<>();
            for (Usuarios u : lista) {
                filas.add(new Object[]{
                        u.getIdUsuario(),
                        u.getNombre(),
                        u.getUsername(),
                        u.getContacto() != null ? u.getContacto() : "-",
                        u.getRol().name(),
                        u.isActivo() ? "Activo" : "Inactivo"
                });
            }
            tabla.setFilas(filas);
        } catch (UsuarioException ex) {
            DialogoConfirmacion.mostrarError(this, ex.getMessage());
        }
    }

    private void crearUsuario() {
        char[] clave = txtClave.getPassword();
        try {
            usuarioServicio.crearUsuario(
                    sesion,
                    txtNombre.getText(),
                    txtContacto.getText(),
                    txtUsername.getText(),
                    clave,
                    (Roles) cboRol.getSelectedItem()
            );
            DialogoConfirmacion.mostrarExito(this, "Usuario creado correctamente.");
            txtNombre.setText("");
            txtContacto.setText("");
            txtUsername.setText("");
            txtClave.setText("");
            cargarTabla();
        } catch (UsuarioException ex) {
            DialogoConfirmacion.mostrarError(this, ex.getMessage());
        } finally {
            Arrays.fill(clave, '\0');
        }
    }

   private void cambiarEstadoSeleccionado() {
    int fila = tabla.getFilaSeleccionada();
    if (fila < 0) {
        DialogoConfirmacion.mostrarAviso(this, "Seleccione un usuario de la tabla.");
        return;
    }

    long idUsuario = (Long) tabla.getValor(fila, 0);
    String username = (String) tabla.getValor(fila, 2);
    boolean activo = "Activo".equals(tabla.getValor(fila, 5));

    String pregunta = activo
            ? "¿Desea desactivar el usuario '" + username + "'?"
            : "¿Desea reactivar el usuario '" + username + "'?";

    if (!DialogoConfirmacion.confirmar(this, pregunta)) {
        return;
    }

    try {
        if (activo) {
            usuarioServicio.desactivarUsuario(sesion, idUsuario);
            DialogoConfirmacion.mostrarExito(this, "Usuario desactivado.");
        } else {
            usuarioServicio.reactivarUsuario(sesion, idUsuario);
            DialogoConfirmacion.mostrarExito(this, "Usuario reactivado.");
        }
        cargarTabla();
    } catch (UsuarioException ex) {
        DialogoConfirmacion.mostrarError(this, ex.getMessage());
    }
}
}