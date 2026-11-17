package com.sigma.datos.dao;

import com.sigma.datos.conexion.Conexion;
import com.sigma.negocio.modelo.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public boolean insertar(Cliente cliente) {
        String sql = "INSERT INTO cliente (nombre, contacto, activo) VALUES (?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String nombreCompleto = (cliente.getNombre() + " " + (cliente.getApellido() != null ? cliente.getApellido() : "")).trim();
            String contacto = construirContacto(cliente);

            pstmt.setString(1, nombreCompleto);
            pstmt.setString(2, contacto);
            pstmt.setBoolean(3, cliente.isActivo());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE cliente SET nombre = ?, contacto = ? WHERE id_cliente = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String nombreCompleto = (cliente.getNombre() + " " + (cliente.getApellido() != null ? cliente.getApellido() : "")).trim();
            String contacto = construirContacto(cliente);

            pstmt.setString(1, nombreCompleto);
            pstmt.setString(2, contacto);
            pstmt.setInt(3, cliente.getId());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean bajaLogica(int id) {
        String sql = "UPDATE cliente SET activo = false WHERE id_cliente = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Cliente> listar() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id_cliente, nombre, contacto, activo FROM cliente WHERE activo = true ORDER BY nombre";
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clientes;
    }

    public Cliente buscarPorDni(String dni) {
        if (dni == null || dni.isBlank()) {
            return null;
        }
        String sql = "SELECT id_cliente, nombre, contacto, activo FROM cliente WHERE activo = true AND contacto LIKE ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%DNI:" + dni.trim() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearCliente(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String construirContacto(Cliente c) {
        List<String> partes = new ArrayList<>();
        if (c.getDni() != null && !c.getDni().isBlank()) partes.add("DNI:" + c.getDni().trim());
        if (c.getTelefono() != null && !c.getTelefono().isBlank()) partes.add(c.getTelefono().trim());
        if (c.getEmail() != null && !c.getEmail().isBlank()) partes.add(c.getEmail().trim());
        return String.join(" | ", partes);
    }

    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_cliente");
        String nombreCompleto = rs.getString("nombre");
        String contacto = rs.getString("contacto");
        boolean activo = rs.getBoolean("activo");

        String nombre = nombreCompleto;
        String apellido = "";
        int espacio = nombreCompleto != null ? nombreCompleto.indexOf(' ') : -1;
        if (espacio > 0) {
            nombre = nombreCompleto.substring(0, espacio);
            apellido = nombreCompleto.substring(espacio + 1);
        }

        String dni = "";
        String telefono = "";
        String email = "";
        if (contacto != null && !contacto.isBlank()) {
            for (String parte : contacto.split("\\|")) {
                String p = parte.trim();
                if (p.startsWith("DNI:")) {
                    dni = p.substring(4).trim();
                } else if (p.contains("@")) {
                    email = p;
                } else {
                    telefono = p;
                }
            }
        }
        return new Cliente(id, nombre, apellido, dni, telefono, email, activo);
    }
}