package com.sigma.datos.dao;

import com.sigma.datos.conexion.Conexion;
import com.sigma.negocio.modelo.TipoVehiculo;
import com.sigma.negocio.modelo.Vehiculo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {

    public boolean insertar(Vehiculo v) {
        String sql = "INSERT INTO vehiculos (patente, marca, modelo, anio, cliente_id, tipo_vehiculo_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, v.getPatente());
            pstmt.setString(2, v.getMarca());
            pstmt.setString(3, v.getModelo());
            pstmt.setInt(4, v.getAnio());
            pstmt.setInt(5, v.getClienteId());
            pstmt.setInt(6, v.getTipoVehiculoId());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Vehiculo buscarPorPatente(String patente) {
        String sql = "SELECT * FROM vehiculos WHERE patente = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patente);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearVehiculo(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Vehiculo> listarTodos() {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM vehiculos";
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearVehiculo(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Vehiculo> listarPorCliente(int clienteId) {
        List<Vehiculo> lista = new ArrayList<>();
        String sql = "SELECT * FROM vehiculos WHERE cliente_id = ?";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, clienteId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearVehiculo(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<TipoVehiculo> listarTiposVehiculo() {
        List<TipoVehiculo> tipos = new ArrayList<>();
        String sql = "SELECT * FROM tipos_vehiculo";
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tipos.add(new TipoVehiculo(rs.getInt("id"), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tipos;
    }

    private Vehiculo mapearVehiculo(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo(
            rs.getInt("id"),
            rs.getString("patente"),
            rs.getString("marca"),
            rs.getString("modelo"),
            rs.getInt("anio"),
            rs.getInt("cliente_id"),
            rs.getInt("tipo_vehiculo_id")
        );
        return v;
    }
}
