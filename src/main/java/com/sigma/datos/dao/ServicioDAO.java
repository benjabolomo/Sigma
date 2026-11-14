package com.sigma.datos.dao;

import com.sigma.datos.conexion.Conexion;
import com.sigma.negocio.modelo.EstadoServicio;
import com.sigma.negocio.modelo.Servicio;
import com.sigma.negocio.modelo.Vehiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Servicio (Órdenes de Servicio).
 * JDBC puro, sentencias preparadas y try-with-resources.
 */
public class ServicioDAO {

    public void insertar(Servicio servicio, Connection conn) throws SQLException {
        String sql = "INSERT INTO servicio (fecha, observacion, estado, patente_vehiculo, id_usuario, " +
                     "total_mano_obra, total_repuestos, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, servicio.getFecha() != null ? Timestamp.valueOf(servicio.getFecha()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, servicio.getObservacion());
            ps.setString(3, servicio.getEstado().name());
            ps.setString(4, servicio.getPatenteVehiculo());
            if (servicio.getIdUsuario() != null) {
                ps.setLong(5, servicio.getIdUsuario());
            } else {
                ps.setNull(5, java.sql.Types.BIGINT);
            }
            ps.setBigDecimal(6, servicio.getTotalManoObra());
            ps.setBigDecimal(7, servicio.getTotalRepuestos());
            ps.setBigDecimal(8, servicio.getTotal());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Error al insertar orden de servicio, ninguna fila fue afectada.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    servicio.setIdServicio(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Error al insertar orden de servicio, no se obtuvo el ID generado.");
                }
            }
        }
    }

    public void actualizar(Servicio servicio, Connection conn) throws SQLException {
        String sql = "UPDATE servicio SET fecha = ?, observacion = ?, estado = ?, patente_vehiculo = ?, " +
                     "total_mano_obra = ?, total_repuestos = ?, total = ? " +
                     "WHERE id_servicio = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, servicio.getFecha() != null ? Timestamp.valueOf(servicio.getFecha()) : Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, servicio.getObservacion());
            ps.setString(3, servicio.getEstado().name());
            ps.setString(4, servicio.getPatenteVehiculo());
            ps.setBigDecimal(5, servicio.getTotalManoObra());
            ps.setBigDecimal(7, servicio.getTotal());
            ps.setBigDecimal(6, servicio.getTotalRepuestos());
            ps.setLong(8, servicio.getIdServicio());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Error al actualizar la orden N° " + servicio.getIdServicio() + ", no fue encontrada.");
            }
        }
    }

    public void actualizarEstado(Long idServicio, EstadoServicio nuevoEstado, Connection conn) throws SQLException {
        String sql = "UPDATE servicio SET estado = ? WHERE id_servicio = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setLong(2, idServicio);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se encontró la orden de servicio con ID: " + idServicio);
            }
        }
    }

    public Servicio buscarPorId(Long idServicio) throws SQLException {
        String sql = "SELECT s.id_servicio, s.fecha, s.observacion, s.estado, s.patente_vehiculo, s.id_usuario, " +
                     "s.total_mano_obra, s.total_repuestos, s.total, " +
                     "v.modelo, v.anio, v.kilometraje, v.id_cliente, v.id_tipo, " +
                     "c.nombre AS nombre_cliente, tv.descripcion AS tipo_descripcion " +
                     "FROM servicio s " +
                     "INNER JOIN vehiculo v ON s.patente_vehiculo = v.patente " +
                     "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                     "LEFT JOIN tipo_vehiculo tv ON v.id_tipo = tv.id_tipo " +
                     "WHERE s.id_servicio = ?";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, idServicio);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearServicio(rs);
                }
            }
        }
        return null;
    }

    public List<Servicio> listarPorVehiculo(String patente) throws SQLException {
        List<Servicio> lista = new ArrayList<>();
        String sql = "SELECT s.id_servicio, s.fecha, s.observacion, s.estado, s.patente_vehiculo, s.id_usuario, " +
                     "s.total_mano_obra, s.total_repuestos, s.total, " +
                     "v.modelo, v.anio, v.kilometraje, v.id_cliente, v.id_tipo, " +
                     "c.nombre AS nombre_cliente, tv.descripcion AS tipo_descripcion " +
                     "FROM servicio s " +
                     "INNER JOIN vehiculo v ON s.patente_vehiculo = v.patente " +
                     "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                     "LEFT JOIN tipo_vehiculo tv ON v.id_tipo = tv.id_tipo " +
                     "WHERE UPPER(TRIM(s.patente_vehiculo)) = UPPER(TRIM(?)) " +
                     "ORDER BY s.fecha DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearServicio(rs));
                }
            }
        }
        return lista;
    }

    public List<Servicio> listarTodos() throws SQLException {
        List<Servicio> lista = new ArrayList<>();
        String sql = "SELECT s.id_servicio, s.fecha, s.observacion, s.estado, s.patente_vehiculo, s.id_usuario, " +
                     "s.total_mano_obra, s.total_repuestos, s.total, " +
                     "v.modelo, v.anio, v.kilometraje, v.id_cliente, v.id_tipo, " +
                     "c.nombre AS nombre_cliente, tv.descripcion AS tipo_descripcion " +
                     "FROM servicio s " +
                     "INNER JOIN vehiculo v ON s.patente_vehiculo = v.patente " +
                     "LEFT JOIN cliente c ON v.id_cliente = c.id_cliente " +
                     "LEFT JOIN tipo_vehiculo tv ON v.id_tipo = tv.id_tipo " +
                     "ORDER BY s.fecha DESC";

        try (Connection conn = Conexion.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearServicio(rs));
            }
        }
        return lista;
    }

    private Servicio mapearServicio(ResultSet rs) throws SQLException {
        Servicio s = new Servicio();
        s.setIdServicio(rs.getLong("id_servicio"));

        Timestamp tsFecha = rs.getTimestamp("fecha");
        if (tsFecha != null) {
            s.setFecha(tsFecha.toLocalDateTime());
        }

        s.setObservacion(rs.getString("observacion"));
        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            try {
                s.setEstado(EstadoServicio.valueOf(estadoStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                s.setEstado(EstadoServicio.PENDIENTE);
            }
        }

        s.setPatenteVehiculo(rs.getString("patente_vehiculo"));
        long idUser = rs.getLong("id_usuario");
        if (!rs.wasNull()) {
            s.setIdUsuario(idUser);
        }

        s.setTotalManoObra(rs.getBigDecimal("total_mano_obra"));
        s.setTotalRepuestos(rs.getBigDecimal("total_repuestos"));
        s.setTotal(rs.getBigDecimal("total"));

        Vehiculo v = new Vehiculo();
        v.setPatente(rs.getString("patente_vehiculo"));
        v.setModelo(rs.getString("modelo"));
        v.setAnio(rs.getInt("anio"));
        v.setKilometraje(rs.getInt("kilometraje"));
        v.setIdCliente(rs.getLong("id_cliente"));
        v.setIdTipo(rs.getInt("id_tipo"));
        v.setNombreCliente(rs.getString("nombre_cliente"));
        v.setTipoDescripcion(rs.getString("tipo_descripcion"));
        s.setVehiculo(v);

        return s;
    }
}
