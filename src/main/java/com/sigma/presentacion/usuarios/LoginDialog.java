package com.sigma.presentacion.usuarios;

import com.sigma.negocio.modelo.Roles;
import com.sigma.negocio.servicios.UsuarioException;
import com.sigma.negocio.servicios.UsuarioServicio;
import com.sigma.presentacion.componentes.DialogoConfirmacion;
import com.sigma.presentacion.componentes.Estilos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public class LoginDialog extends JDialog {

    private final UsuarioServicio usuarioServicio;
    private UsuarioServicio.Sesion sesionIniciada;

    private JTextField txtUsuario;
    private JPasswordField txtClave;

    public LoginDialog(Frame owner) {
        super(owner, "Sigma - Iniciar Sesión", true);
        this.usuarioServicio = new UsuarioServicio();
        inicializarComponentes();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void inicializarComponentes() {
        JPanel principal = new JPanel(new BorderLayout(12, 12));
        principal.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel cabecera = new JPanel(new GridLayout(2, 1, 2, 2));
        JLabel lblTitulo = new JLabel("SIGMA - Taller Mecánico", SwingConstants.CENTER);
        lblTitulo.setFont(Estilos.TITULO);
        lblTitulo.setForeground(Estilos.PRIMARIO);
        JLabel lblSub = new JLabel("Ingrese sus credenciales para continuar", SwingConstants.CENTER);
        lblSub.setFont(Estilos.PEQUENA);
        cabecera.add(lblTitulo);
        cabecera.add(lblSub);
        principal.add(cabecera, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Usuario:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtUsuario = new JTextField(18);
        form.add(txtUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(new JLabel("Contraseña:"), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        txtClave = new JPasswordField(18);
        form.add(txtClave, gbc);

        principal.add(form, BorderLayout.CENTER);

        JPanel botones = new JPanel(new BorderLayout(8, 8));
        JPanel filaPrincipal = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnIngresar = Estilos.botonPrimario("Ingresar");
        JButton btnSalir = Estilos.botonSecundario("Salir");
        btnIngresar.addActionListener(e -> intentarLogin());
        btnSalir.addActionListener(e -> dispose());
        filaPrincipal.add(btnIngresar);
        filaPrincipal.add(btnSalir);

     botones.add(filaPrincipal, BorderLayout.NORTH);
        if (!existeAdministrador()) {
            JButton btnConfigAdmin = Estilos.botonSecundario("Crear Administrador Inicial");
            btnConfigAdmin.setFont(Estilos.PEQUENA);
            btnConfigAdmin.addActionListener(e -> mostrarDialogoAdminInicial());
            botones.add(btnConfigAdmin, BorderLayout.SOUTH);
        }
        principal.add(botones, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnIngresar);
        setContentPane(principal);
    }

    private void intentarLogin() {
        String user = txtUsuario.getText().trim();
        char[] pass = txtClave.getPassword();
        try {
            sesionIniciada = usuarioServicio.iniciarSesion(user, pass);
            dispose();
        } catch (UsuarioException ex) {
            DialogoConfirmacion.mostrarError(this, ex.getMessage());
            txtClave.setText("");
            txtClave.requestFocusInWindow();
        } finally {
            Arrays.fill(pass, '\0');
        }
    }

    private void mostrarDialogoAdminInicial() {
        JTextField txtNombre = new JTextField();
        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JPasswordField txtPass2 = new JPasswordField();

        JPanel pnl = new JPanel(new GridLayout(4, 2, 6, 6));
        pnl.add(new JLabel("Nombre completo:"));
        pnl.add(txtNombre);
        pnl.add(new JLabel("Nombre de usuario:"));
        pnl.add(txtUser);
        pnl.add(new JLabel("Contraseña:"));
        pnl.add(txtPass);
        pnl.add(new JLabel("Repetir contraseña:"));
        pnl.add(txtPass2);

        int res = JOptionPane.showConfirmDialog(this, pnl, "Configurar Administrador Inicial",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            char[] c1 = txtPass.getPassword();
            char[] c2 = txtPass2.getPassword();
            try {
                if (!Arrays.equals(c1, c2)) {
                    DialogoConfirmacion.mostrarAviso(this, "Las contraseñas no coinciden.");
                    return;
                }
                // Usa un helper interno mediante reflexión controlada o crea vía servicio
                CrearAdminHelper.crear(usuarioServicio, txtNombre.getText(), txtUser.getText(), c1);
                DialogoConfirmacion.mostrarExito(this, "Administrador creado. Ya podés iniciar sesión.");
                txtUsuario.setText(txtUser.getText().trim());
                txtClave.requestFocusInWindow();
            } catch (UsuarioException ex) {
                DialogoConfirmacion.mostrarError(this, ex.getMessage());
            } finally {
                Arrays.fill(c1, '\0');
                Arrays.fill(c2, '\0');
            }
        }
    }

    private boolean existeAdministrador() {
        String sql = "SELECT 1 FROM usuario u JOIN roles r ON u.id_rol = r.id_rol WHERE r.nombre_rol = 'ADMINISTRADOR' LIMIT 1";
        try (java.sql.Connection conn = com.sigma.datos.conexion.Conexion.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            return rs.next();
        } catch (Exception e) {
            return true;
        }
    }
    
    public UsuarioServicio.Sesion getSesionIniciada() {
        return sesionIniciada;
    }

    // Puente para invocar crearAdministradorInicial (package-private en negocio.servicios)
    private static final class CrearAdminHelper {
        static void crear(UsuarioServicio servicio, String nombre, String username, char[] clave) throws UsuarioException {
            try {
                var m = UsuarioServicio.class.getDeclaredMethod("crearAdministradorInicial", String.class, String.class, char[].class);
                m.setAccessible(true);
                m.invoke(servicio, nombre, username, clave);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                if (ite.getCause() instanceof UsuarioException ue) {
                    throw ue;
                }
                throw new RuntimeException(ite.getCause());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }
}