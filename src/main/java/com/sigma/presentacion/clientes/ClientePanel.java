/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigma.presentacion.clientes;



import com.sigma.negocio.modelo.Cliente;
import com.sigma.negocio.servicios.ClienteServicio;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ClientePanel extends JPanel {
    private final ClienteServicio clienteServicio;

    public ClientePanel() {
        this.clienteServicio = new ClienteServicio();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        JLabel lblTitulo = new JLabel("Gestión de Clientes (Ejemplo Base)", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblTitulo, BorderLayout.NORTH);

        JButton btnListar = new JButton("Probar consulta de clientes");
        btnListar.addActionListener(e -> probarConsulta());
        add(btnListar, BorderLayout.CENTER);
    }

    private void probarConsulta() {
        List<Cliente> clientes = clienteServicio.obtenerClientes();
        JOptionPane.showMessageDialog(this, "Clientes encontrados en BD: " + clientes.size());
    }
}
