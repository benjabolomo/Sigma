/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigma.datos.dao;

/*clase de ejemplo para poder manter estructura en el git.
ademas tiene un ejemplo de como usar Conexion.getConnection()*/

import com.sigma.datos.conexion.Conexion;
import com.sigma.negocio.modelo.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {
    /**
     * Inserta un nuevo cliente en la base de datos.
     */
    public boolean insertar(Cliente cliente) {
        String sql = "INSERT INTO cliente (nombre, contacto) VALUES (?, ?)";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getContacto());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        cliente.setIdCliente(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error en ClienteDAO.insertar: " + e.getMessage());
        }
        return false;
    }

    /**
     * Obtiene la lista completa de clientes.
     */
    public List<Cliente> listarTodos() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id_cliente, nombre, contacto, fecha_creacion FROM cliente ORDER BY nombre ASC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente(
                    rs.getLong("id_cliente"),
                    rs.getString("nombre"),
                    rs.getString("contacto"),
                    rs.getTimestamp("fecha_creacion")
                );
                clientes.add(cliente);
            }
        } catch (SQLException e) {
            System.err.println("Error en ClienteDAO.listarTodos: " + e.getMessage());
        }
        return clientes;
    }
}
