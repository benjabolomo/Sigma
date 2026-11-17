package com.sigma.datos.dao;

import com.sigma.datos.conexion.Conexion;
import com.sigma.negocio.modelo.TipoVehiculo;
import com.sigma.negocio.modelo.Vehiculo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {

    public boolean insertar(Vehiculo v) {
        String sql = "INSERT INTO vehiculo (patente, modelo, anio, kilometraje, id_cliente, id_tipo) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String marcaYModelo = ((v.getMarca() != null ? v.getMarca() : "") + " " + (v.getModelo() != null ? v.getModelo() : "")).trim();

            pstmt.setString(1, v.getPatente().trim().toUpperCase());
            pstmt.setString(2, marcaYModelo);
            pstmt.setInt(3, v.getAnio());
            pstmt.setInt(4, v.getKilometraje());
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
        String sql = "SELECT v.*, c.nombre AS nombre_cliente, tv.descripcion AS tipo_descripcion " +
                     "FROM vehiculo v " +
                     "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                     "LEFT JOIN tipo_vehiculo tv ON v.id_tipo = tv.id_tipo " +
                     "WHERE UPPER(TRIM(v.patente)) = UPPER(TRIM(?))";
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
        String sql = "SELECT v.*, c.nombre AS nombre_cliente, tv.descripcion AS tipo_descripcion " +
                     "FROM vehiculo v " +
                     "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                     "LEFT JOIN tipo_vehiculo tv ON v.id_tipo = tv.id_tipo " +
                     "ORDER BY v.patente";
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
        String sql = "SELECT v.*, c.nombre AS nombre_cliente, tv.descripcion AS tipo_descripcion " +
                     "FROM vehiculo v " +
                     "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                     "LEFT JOIN tipo_vehiculo tv ON v.id_tipo = tv.id_tipo " +
                     "WHERE v.id_cliente = ?";
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
        String sql = "SELECT id_tipo, descripcion FROM tipo_vehiculo ORDER BY id_tipo";
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tipos.add(new TipoVehiculo(rs.getInt("id_tipo"), rs.getString("descripcion")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tipos;
    }

    private Vehiculo mapearVehiculo(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo(
            0,
            rs.getString("patente"),
            "",
            rs.getString("modelo"),
            rs.getInt("anio"),
            rs.getInt("id_cliente"),
            rs.getInt("id_tipo")
        );
        v.setKilometraje(rs.getInt("kilometraje"));
        v.setNombreCliente(rs.getString("nombre_cliente"));
        v.setTipoDescripcion(rs.getString("tipo_descripcion"));
        return v;
    }
}
