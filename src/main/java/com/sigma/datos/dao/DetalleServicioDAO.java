package com.sigma.datos.dao;

import com.sigma.datos.conexion.Conexion;
import com.sigma.negocio.modelo.DetalleServicio;
import com.sigma.negocio.modelo.TipoDetalle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad DetalleServicio.
 * JDBC puro, sentencias preparadas y try-with-resources.
 */
public class DetalleServicioDAO {

    public void insertar(DetalleServicio detalle, Connection conn) throws SQLException {
        String sql = "INSERT INTO detalle_servicio (id_servicio, tipo, descripcion, cantidad, precio_unitario, subtotal) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, detalle.getIdServicio());
            ps.setString(2, detalle.getTipo().name());
            ps.setString(3, detalle.getDescripcion());
            ps.setBigDecimal(4, detalle.getCantidad());
            ps.setBigDecimal(5, detalle.getPrecioUnitario());
            ps.setBigDecimal(6, detalle.getSubtotal());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Error al insertar detalle de servicio, ninguna fila fue afectada.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    detalle.setIdDetalle(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Error al insertar detalle de servicio, no se obtuvo el ID generado.");
                }
            }
        }
    }

    public void eliminarPorServicio(Long idServicio, Connection conn) throws SQLException {
        String sql = "DELETE FROM detalle_servicio WHERE id_servicio = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idServicio);
            ps.executeUpdate();
        }
    }

    public List<DetalleServicio> listarPorServicio(Long idServicio) throws SQLException {
        try (Connection conn = Conexion.getConnection()) {
            return listarPorServicio(idServicio, conn);
        }
    }

    public List<DetalleServicio> listarPorServicio(Long idServicio, Connection conn) throws SQLException {
        List<DetalleServicio> lista = new ArrayList<>();
        String sql = "SELECT id_detalle, id_servicio, tipo, descripcion, cantidad, precio_unitario, subtotal " +
                     "FROM detalle_servicio WHERE id_servicio = ? ORDER BY id_detalle ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idServicio);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleServicio det = new DetalleServicio();
                    det.setIdDetalle(rs.getLong("id_detalle"));
                    det.setIdServicio(rs.getLong("id_servicio"));
                    det.setTipo(TipoDetalle.valueOf(rs.getString("tipo")));
                    det.setDescripcion(rs.getString("descripcion"));
                    det.setCantidad(rs.getBigDecimal("cantidad"));
                    det.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    det.setSubtotal(rs.getBigDecimal("subtotal"));
                    lista.add(det);
                }
            }
        }
        return lista;
    }
}
