/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigma.presentacion.vehiculos;

import com.sigma.negocio.modelo.Cliente;
import com.sigma.negocio.modelo.TipoVehiculo;
import com.sigma.negocio.modelo.Vehiculo;
import com.sigma.negocio.servicios.ClienteServicio;
import com.sigma.negocio.servicios.VehiculoServicio;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VehiculoPanel extends JPanel {
    private JTextField txtPatente, txtMarca, txtModelo, txtAnio;
    private JComboBox<Cliente> cmbClientes;
    private JComboBox<TipoVehiculo> cmbTipos;
    private JButton btnRegistrar;
    
    private VehiculoServicio vehiculoServicio;
    private ClienteServicio clienteServicio;

    public VehiculoPanel() {
        vehiculoServicio = new VehiculoServicio();
        clienteServicio = new ClienteServicio();
        
        setLayout(new GridLayout(7, 2, 10, 10));

        add(new JLabel("Patente:"));
        txtPatente = new JTextField();
        add(txtPatente);

        add(new JLabel("Marca:"));
        txtMarca = new JTextField();
        add(txtMarca);

        add(new JLabel("Modelo:"));
        txtModelo = new JTextField();
        add(txtModelo);

        add(new JLabel("Año:"));
        txtAnio = new JTextField();
        add(txtAnio);

        add(new JLabel("Cliente:"));
        cmbClientes = new JComboBox<>();
        cargarClientes();
        add(cmbClientes);

        add(new JLabel("Tipo de Vehículo:"));
        cmbTipos = new JComboBox<>();
        cargarTipos();
        add(cmbTipos);

        btnRegistrar = new JButton("Registrar Vehículo");
        add(btnRegistrar);

        btnRegistrar.addActionListener(e -> registrarVehiculo());
    }

    private void cargarClientes() {
        List<Cliente> clientes = clienteServicio.listarClientes();
        for (Cliente c : clientes) {
            cmbClientes.addItem(c);
        }
    }

    private void cargarTipos() {
        List<TipoVehiculo> tipos = vehiculoServicio.listarTiposVehiculo();
        for (TipoVehiculo t : tipos) {
            cmbTipos.addItem(t);
        }
    }

    private void registrarVehiculo() {
        try {
            String patente = txtPatente.getText().trim();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            int anio = Integer.parseInt(txtAnio.getText().trim());
            
            Cliente clienteSeleccionado = (Cliente) cmbClientes.getSelectedItem();
            TipoVehiculo tipoSeleccionado = (TipoVehiculo) cmbTipos.getSelectedItem();

            if (patente.isEmpty() || marca.isEmpty() || modelo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Vehiculo v = new Vehiculo();
            v.setPatente(patente);
            v.setMarca(marca);
            v.setModelo(modelo);
            v.setAnio(anio);
            v.setClienteId(clienteSeleccionado.getId());
            v.setTipoVehiculoId(tipoSeleccionado.getId());

            boolean exito = vehiculoServicio.registrarVehiculo(v);
            if (exito) {
                JOptionPane.showMessageDialog(this, "Vehículo registrado con éxito.");
                txtPatente.setText("");
                txtMarca.setText("");
                txtModelo.setText("");
                txtAnio.setText("");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El año debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.ERROR_MESSAGE);
        }
    }
}
