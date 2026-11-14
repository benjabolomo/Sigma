package com.sigma.negocio.servicios;

import com.sigma.datos.conexion.Conexion;
import com.sigma.datos.dao.DetalleServicioDAO;
import com.sigma.datos.dao.ServicioDAO;
import com.sigma.negocio.modelo.DetalleServicio;
import com.sigma.negocio.modelo.EstadoServicio;
import com.sigma.negocio.modelo.Servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Repositorio de la capa de negocio con control manual y estricto de transacciones JDBC.
 * Si falla un detalle, la orden entera se revierte (rollback) para mantener la consistencia.
 */
public class ServicioRepository {

    private final ServicioDAO servicioDAO;
    private final DetalleServicioDAO detalleServicioDAO;

    public ServicioRepository() {
        this.servicioDAO = new ServicioDAO();
        this.detalleServicioDAO = new DetalleServicioDAO();
    }

    public ServicioRepository(ServicioDAO servicioDAO, DetalleServicioDAO detalleServicioDAO) {
        this.servicioDAO = servicioDAO;
        this.detalleServicioDAO = detalleServicioDAO;
    }

    /**
     * Guarda atómicamente la orden junto con sus detalles bajo control transaccional manual.
     */
    public void guardarOrdenConDetalles(Servicio servicio) throws SQLException {
        if (servicio == null) {
            throw new IllegalArgumentException("La orden de servicio no puede ser nula.");
        }

        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false); // 1. Deshabilitar autoCommit

            boolean esNuevo = (servicio.getIdServicio() == null || servicio.getIdServicio() == 0);
            if (esNuevo) {
                servicioDAO.insertar(servicio, conn);
            } else {
                servicioDAO.actualizar(servicio, conn);
                detalleServicioDAO.eliminarPorServicio(servicio.getIdServicio(), conn);
            }

            List<DetalleServicio> detalles = servicio.getDetalles();
            if (detalles != null && !detalles.isEmpty()) {
                for (DetalleServicio detalle : detalles) {
                    detalle.setIdServicio(servicio.getIdServicio());
                    detalleServicioDAO.insertar(detalle, conn); // Si un detalle falla...
                }
            }

            conn.commit(); // 2. Confirmación total

        } catch (SQLException | RuntimeException ex) {
            // CONTROL DE FALLOS: Si falla cualquier detalle o la orden, rollback total
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
                Conexion.cerrarConexion(conn);
            }
        }
    }

    public void actualizarEstado(Long idServicio, EstadoServicio nuevoEstado) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexion.getConnection();
            conn.setAutoCommit(false);

            servicioDAO.actualizarEstado(idServicio, nuevoEstado, conn);

            conn.commit();
        } catch (SQLException | RuntimeException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
            }
            throw ex;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
                Conexion.cerrarConexion(conn);
            }
        }
    }

    public Servicio buscarPorId(Long idServicio) throws SQLException {
        Servicio servicio = servicioDAO.buscarPorId(idServicio);
        if (servicio != null) {
            List<DetalleServicio> detalles = detalleServicioDAO.listarPorServicio(idServicio);
            servicio.setDetalles(detalles);
        }
        return servicio;
    }

    public List<Servicio> listarPorVehiculo(String patente) throws SQLException {
        return servicioDAO.listarPorVehiculo(patente);
    }

    public List<Servicio> listarTodos() throws SQLException {
        return servicioDAO.listarTodos();
    }
}
