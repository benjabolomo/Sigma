/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigma.presentacion.clientes;

import com.sigma.negocio.modelo.Cliente;
import com.sigma.negocio.servicios.ClienteServicio;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientePanel extends JPanel {
    private final ClienteServicio clienteServicio;
    private JTextField txtDni, txtNombre, txtApellido, txtTelefono, txtEmail;
    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
    private int clienteIdSeleccionado = -1;

    public ClientePanel() {
        this.clienteServicio = new ClienteServicio();
        setLayout(new BorderLayout(10, 10));
        initUI();
        cargarTabla();
    }

    private void initUI() {
        // --- PANEL DE FORMULARIO (NORTE) ---
        JPanel panelForm = new JPanel(new GridLayout(5, 2, 5, 5));
        panelForm.setBorder(BorderFactory.createTitledBorder("Gestión de Clientes - Formulario"));

        panelForm.add(new JLabel("DNI:"));
        txtDni = new JTextField();
        panelForm.add(txtDni);

        panelForm.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelForm.add(txtNombre);

        panelForm.add(new JLabel("Apellido:"));
        txtApellido = new JTextField();
        panelForm.add(txtApellido);

        panelForm.add(new JLabel("Teléfono:"));
        txtTelefono = new JTextField();
        panelForm.add(txtTelefono);

        panelForm.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        panelForm.add(txtEmail);

        // --- BOTONES DE ACCIÓN ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnBaja = new JButton("Dar de baja");
        JButton btnLimpiar = new JButton("Limpiar");

        panelBotones.add(btnGuardar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnBaja);
        panelBotones.add(btnLimpiar);

        JPanel nortePanel = new JPanel(new BorderLayout());
        nortePanel.add(panelForm, BorderLayout.CENTER);
        nortePanel.add(panelBotones, BorderLayout.SOUTH);
        add(nortePanel, BorderLayout.NORTH);

        // --- TABLA DE CONSULTA (CENTRO) ---
        modeloTabla = new DefaultTableModel(new Object[]{"ID", "DNI", "Nombre", "Apellido", "Teléfono", "Email"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // La tabla no se edita directamente haciendo clic en las celdas
            }
        };
        tablaClientes = new JTable(modeloTabla);
        add(new JScrollPane(tablaClientes), BorderLayout.CENTER);

        // --- EVENTOS (LISTENERS) ---
        btnGuardar.addActionListener(e -> guardarCliente());
        btnModificar.addActionListener(e -> modificarCliente());
        btnBaja.addActionListener(e -> bajaCliente());
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        // Al hacer clic en una fila de la tabla, cargamos los datos en los campos para modificar
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaClientes.getSelectedRow() != -1) {
                int fila = tablaClientes.getSelectedRow();
                clienteIdSeleccionado = (int) modeloTabla.getValueAt(fila, 0);
                txtDni.setText((String) modeloTabla.getValueAt(fila, 1));
                txtNombre.setText((String) modeloTabla.getValueAt(fila, 2));
                txtApellido.setText((String) modeloTabla.getValueAt(fila, 3));
                txtTelefono.setText((String) modeloTabla.getValueAt(fila, 4));
                txtEmail.setText((String) modeloTabla.getValueAt(fila, 5));
            }
        });
    }

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        List<Cliente> clientes = clienteServicio.listarClientes();
        for (Cliente c : clientes) {
            modeloTabla.addRow(new Object[]{
                c.getId(), c.getDni(), c.getNombre(), c.getApellido(), c.getTelefono(), c.getEmail()
            });
        }
    }

    private void guardarCliente() {
        try {
            Cliente c = new Cliente();
            c.setDni(txtDni.getText().trim());
            c.setNombre(txtNombre.getText().trim());
            c.setApellido(txtApellido.getText().trim());
            c.setTelefono(txtTelefono.getText().trim());
            c.setEmail(txtEmail.getText().trim());

            if (c.getDni().isEmpty() || c.getNombre().isEmpty() || c.getApellido().isEmpty()) {
                JOptionPane.showMessageDialog(this, "DNI, Nombre y Apellido son obligatorios.", "Error de validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean exito = clienteServicio.registrarCliente(c);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Cliente registrado con éxito.");
                cargarTabla();
                limpiarFormulario();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarCliente() {
        if (clienteIdSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla para modificar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Cliente c = new Cliente();
            c.setId(clienteIdSeleccionado);
            c.setDni(txtDni.getText().trim());
            c.setNombre(txtNombre.getText().trim());
            c.setApellido(txtApellido.getText().trim());
            c.setTelefono(txtTelefono.getText().trim());
            c.setEmail(txtEmail.getText().trim());

            boolean exito = clienteServicio.actualizarCliente(c);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Cliente modificado con éxito.");
                cargarTabla();
                limpiarFormulario();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void bajaCliente() {
        if (clienteIdSeleccionado == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla para dar de baja.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de realizar la baja lógica de este cliente?", "Confirmar baja", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean exito = clienteServicio.bajaLogicaCliente(clienteIdSeleccionado);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Cliente dado de baja correctamente.");
                cargarTabla();
                limpiarFormulario();
            }
        }
    }

    private void limpiarFormulario() {
        clienteIdSeleccionado = -1;
        txtDni.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtTelefono.setText("");
        txtEmail.setText("");
        tablaClientes.clearSelection();
    }
}
