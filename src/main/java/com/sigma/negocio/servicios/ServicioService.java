package com.sigma.negocio.servicios;

import com.sigma.datos.dao.VehiculoDAO;
import com.sigma.negocio.modelo.DetalleServicio;
import com.sigma.negocio.modelo.EstadoServicio;
import com.sigma.negocio.modelo.Servicio;
import com.sigma.negocio.modelo.TipoDetalle;
import com.sigma.negocio.modelo.Vehiculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de negocio para la gestión integral de órdenes de servicio.
 * Responsable de cálculos financieros, validación de transiciones de estado
 * y consultas de historial por vehículo.
 */
public class ServicioService {

    private final ServicioRepository repository;
    private final VehiculoDAO vehiculoDAO;

    public ServicioService() {
        this.repository = new ServicioRepository();
        this.vehiculoDAO = new VehiculoDAO();
    }

    public ServicioService(ServicioRepository repository, VehiculoDAO vehiculoDAO) {
        this.repository = repository;
        this.vehiculoDAO = vehiculoDAO;
    }

    public void registrarOrden(Servicio servicio) throws ServicioException {
        validarServicio(servicio);

        if (servicio.getEstado() == null) {
            servicio.setEstado(EstadoServicio.PENDIENTE);
        }
        if (servicio.getFecha() == null) {
            servicio.setFecha(LocalDateTime.now());
        }

        calcularTotales(servicio);

        try {
            repository.guardarOrdenConDetalles(servicio);
        } catch (SQLException e) {
            throw new ServicioException("Error al registrar la orden de servicio: " + e.getMessage(), e);
        }
    }

    public void actualizarOrden(Servicio servicio) throws ServicioException {
        if (servicio == null || servicio.getIdServicio() == null || servicio.getIdServicio() <= 0) {
            throw new ServicioException("Debe especificar una orden válida para actualizar.");
        }

        Servicio existente;
        try {
            existente = repository.buscarPorId(servicio.getIdServicio());
        } catch (SQLException e) {
            throw new ServicioException("Error al consultar la orden existente: " + e.getMessage(), e);
        }

        if (existente == null) {
            throw new ServicioException("La orden de servicio N° " + servicio.getIdServicio() + " no existe.");
        }

        if (existente.getEstado() == EstadoServicio.FINALIZADO || existente.getEstado() == EstadoServicio.CANCELADO) {
            throw new ServicioException("No es posible modificar una orden en estado " + existente.getEstado().getNombreVisible() + ".");
        }

        validarServicio(servicio);
        calcularTotales(servicio);

        try {
            repository.guardarOrdenConDetalles(servicio);
        } catch (SQLException e) {
            throw new ServicioException("Error al actualizar la orden de servicio: " + e.getMessage(), e);
        }
    }

    public void cambiarEstado(Long idServicio, EstadoServicio nuevoEstado) throws ServicioException {
        if (nuevoEstado == null) {
            throw new ServicioException("El nuevo estado no puede ser nulo.");
        }

        Servicio servicio;
        try {
            servicio = repository.buscarPorId(idServicio);
        } catch (SQLException e) {
            throw new ServicioException("Error al recuperar la orden: " + e.getMessage(), e);
        }

        if (servicio == null) {
            throw new ServicioException("La orden de servicio N° " + idServicio + " no existe.");
        }

        EstadoServicio estadoActual = servicio.getEstado();
        if (estadoActual == nuevoEstado) {
            return;
        }

        if (!estadoActual.esTransicionValida(nuevoEstado)) {
            throw new ServicioException(String.format(
                    "Transición de estado inválida: No se puede cambiar de '%s' a '%s'.",
                    estadoActual.getNombreVisible(), nuevoEstado.getNombreVisible()
            ));
        }

        if (nuevoEstado == EstadoServicio.CONFIRMADO || nuevoEstado == EstadoServicio.EN_PROCESO) {
            if (servicio.getDetalles() == null || servicio.getDetalles().isEmpty()) {
                throw new ServicioException("No se puede avanzar una orden sin renglones de mano de obra o repuestos cargados.");
            }
        }

        try {
            repository.actualizarEstado(idServicio, nuevoEstado);
            servicio.setEstado(nuevoEstado);
        } catch (SQLException e) {
            throw new ServicioException("Error al actualizar el estado: " + e.getMessage(), e);
        }
    }

    public void calcularTotales(Servicio servicio) {
        if (servicio == null) {
            return;
        }

        BigDecimal acumuladoManoObra = BigDecimal.ZERO;
        BigDecimal acumuladoRepuestos = BigDecimal.ZERO;

        List<DetalleServicio> detalles = servicio.getDetalles();
        if (detalles != null) {
            for (DetalleServicio det : detalles) {
                if (det == null) continue;
                BigDecimal sub = det.calcularSubtotal();
                if (det.getTipo() == TipoDetalle.MANO_DE_OBRA) {
                    acumuladoManoObra = acumuladoManoObra.add(sub);
                } else if (det.getTipo() == TipoDetalle.REPUESTO) {
                    acumuladoRepuestos = acumuladoRepuestos.add(sub);
                }
            }
        }

        servicio.setTotalManoObra(acumuladoManoObra.setScale(2, RoundingMode.HALF_UP));
        servicio.setTotalRepuestos(acumuladoRepuestos.setScale(2, RoundingMode.HALF_UP));
        servicio.setTotal(acumuladoManoObra.add(acumuladoRepuestos).setScale(2, RoundingMode.HALF_UP));
    }

    public List<Servicio> consultarHistorialPorPatente(String patente) throws ServicioException {
        if (patente == null || patente.trim().isEmpty()) {
            throw new ServicioException("Debe ingresar una patente para buscar el historial.");
        }
        try {
            return repository.listarPorVehiculo(patente.trim().toUpperCase());
        } catch (SQLException e) {
            throw new ServicioException("Error al consultar el historial: " + e.getMessage(), e);
        }
    }

    public Servicio obtenerPorId(Long idServicio) throws ServicioException {
        try {
            Servicio s = repository.buscarPorId(idServicio);
            if (s == null) {
                throw new ServicioException("No existe la orden N° " + idServicio);
            }
            return s;
        } catch (SQLException e) {
            throw new ServicioException("Error al consultar la orden N° " + idServicio + ": " + e.getMessage(), e);
        }
    }

    public List<Servicio> listarTodas() throws ServicioException {
        try {
            return repository.listarTodos();
        } catch (SQLException e) {
            throw new ServicioException("Error al listar órdenes: " + e.getMessage(), e);
        }
    }

    private void validarServicio(Servicio servicio) throws ServicioException {
        if (servicio == null) {
            throw new ServicioException("La orden de servicio no puede ser nula.");
        }
        if (servicio.getPatenteVehiculo() == null || servicio.getPatenteVehiculo().trim().isEmpty()) {
            throw new ServicioException("Debe asociar un vehículo con patente válida a la orden.");
        }
        if (servicio.getObservacion() == null || servicio.getObservacion().trim().isEmpty()) {
            throw new ServicioException("Debe ingresar la observación o motivo de ingreso.");
        }

        List<DetalleServicio> detalles = servicio.getDetalles();
        if (detalles != null) {
            for (int i = 0; i < detalles.size(); i++) {
                DetalleServicio det = detalles.get(i);
                int fila = i + 1;
                if (det.getTipo() == null) {
                    throw new ServicioException("Renglón " + fila + ": Especifique Mano de Obra o Repuesto.");
                }
                if (det.getDescripcion() == null || det.getDescripcion().trim().isEmpty()) {
                    throw new ServicioException("Renglón " + fila + ": Ingrese una descripción para el ítem.");
                }
                if (det.getCantidad() == null || det.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ServicioException("Renglón " + fila + ": La cantidad debe ser mayor a cero.");
                }
                if (det.getPrecioUnitario() == null || det.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ServicioException("Renglón " + fila + ": El precio unitario no puede ser negativo.");
                }
            }
        }
    }
}
