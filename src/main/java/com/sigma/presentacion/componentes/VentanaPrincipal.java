package com.sigma.presentacion.componentes;

import com.sigma.negocio.modelo.Permiso;
import com.sigma.negocio.modelo.Roles;
import com.sigma.negocio.modelo.Usuarios;
import com.sigma.negocio.servicios.UsuarioServicio;
import com.sigma.presentacion.clientes.ClientePanel;
import com.sigma.presentacion.componentes.Estilos;
import com.sigma.presentacion.reportes.ReportePanel;
import com.sigma.presentacion.servicios.ServicioPanel;
import com.sigma.presentacion.usuarios.LoginDialog;
import com.sigma.presentacion.usuarios.UsuarioPanel;
import com.sigma.presentacion.vehiculos.VehiculoPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private final UsuarioServicio.Sesion sesion;

    public VentanaPrincipal(UsuarioServicio.Sesion sesion) {
        this.sesion = sesion;
        Usuarios u = sesion.getUsuario();

        setTitle("SIGMA - Sistema de Gestión de Taller Mecánico");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 740);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());

        // Barra superior con usuario logueado, rol y botón cerrar sesión
        JPanel barraSuperior = new JPanel(new BorderLayout());
        barraSuperior.setBackground(Estilos.FONDO_SUAVE);
        barraSuperior.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Estilos.BORDE),
                new EmptyBorder(8, 14, 8, 14)
        ));

        JLabel lblMarca = new JLabel("SIGMA | Gestión Integral de Taller");
        lblMarca.setFont(Estilos.TITULO);
        lblMarca.setForeground(Estilos.PRIMARIO);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        derecha.setOpaque(false);
        JLabel lblUsuario = new JLabel("Usuario: " + u.getNombre() + " (" + u.getRol().name() + ")");
        lblUsuario.setFont(Estilos.SECCION);

        JButton btnCerrarSesion = Estilos.botonSecundario("Cerrar Sesión");
        btnCerrarSesion.addActionListener(e -> {
            dispose();
            iniciar();
        });

        derecha.add(lblUsuario);
        derecha.add(btnCerrarSesion);

        barraSuperior.add(lblMarca, BorderLayout.WEST);
        barraSuperior.add(derecha, BorderLayout.EAST);
        root.add(barraSuperior, BorderLayout.NORTH);

        // Pestañas según permisos del rol
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Estilos.SECCION);
        Roles rol = u.getRol();

        if (rol.tienePermiso(Permiso.GESTIONAR_SERVICIOS)) {
            tabs.addTab("Órdenes de Servicio", new ServicioPanel());
        }
        if (rol.tienePermiso(Permiso.GESTIONAR_CLIENTES)) {
            tabs.addTab("Clientes", new ClientePanel());
        }
        if (rol.tienePermiso(Permiso.GESTIONAR_VEHICULOS)) {
            tabs.addTab("Vehículos", new VehiculoPanel());
        }
        if (rol == Roles.ADMINISTRADOR || rol == Roles.RECEPCIONISTA) {
            tabs.addTab("Reportes e Ingresos", new ReportePanel());
        }
        if (rol.tienePermiso(Permiso.GESTIONAR_USUARIOS)) {
            tabs.addTab("Usuarios y Permisos", new UsuarioPanel(sesion));
        }

        tabs.addChangeListener(e -> {
            Component seleccionada = tabs.getSelectedComponent();
            if (seleccionada instanceof VehiculoPanel vp) {
                vp.refrescarCombos();
            } else if (seleccionada instanceof ServicioPanel sp) {
                sp.refrescarVista();
            } else if (seleccionada instanceof ReportePanel rp) {
                rp.generarReporte();
            }
        });

        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    public static void iniciar() {
        LoginDialog login = new LoginDialog(null);
        login.setVisible(true);

        UsuarioServicio.Sesion sesion = login.getSesionIniciada();
        if (sesion != null) {
            new VentanaPrincipal(sesion).setVisible(true);
        } else {
            System.exit(0);
        }
    }
}