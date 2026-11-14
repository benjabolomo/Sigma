package com.sigma.negocio.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidad principal que representa una orden de servicio en el taller mecánico.
 * Vincula el vehículo por patente, los estados del ciclo de vida y la lista de renglones.
 */
public class Servicio {

    private Long idServicio;
    private LocalDateTime fecha;
    private String observacion;
    private EstadoServicio estado;
    private String patenteVehiculo;
    private Vehiculo vehiculo;
    private Long idUsuario;
    private final List<DetalleServicio> detalles = new ArrayList<>();
    private BigDecimal totalManoObra;
    private BigDecimal totalRepuestos;
    private BigDecimal total;

    public Servicio() {
        this.fecha = LocalDateTime.now();
        this.estado = EstadoServicio.PENDIENTE;
        this.totalManoObra = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.totalRepuestos = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public Servicio(String patenteVehiculo, String observacion) {
        this();
        this.patenteVehiculo = patenteVehiculo;
        this.observacion = observacion;
    }

    public Servicio(Long idServicio, LocalDateTime fecha, String observacion, EstadoServicio estado,
                    String patenteVehiculo, Long idUsuario, BigDecimal totalManoObra,
                    BigDecimal totalRepuestos, BigDecimal total) {
        this.idServicio = idServicio;
        this.fecha = fecha != null ? fecha : LocalDateTime.now();
        this.observacion = observacion;
        this.estado = estado != null ? estado : EstadoServicio.PENDIENTE;
        this.patenteVehiculo = patenteVehiculo;
        this.idUsuario = idUsuario;
        this.totalManoObra = totalManoObra != null ? totalManoObra : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.totalRepuestos = totalRepuestos != null ? totalRepuestos : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.total = total != null ? total : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public void agregarDetalle(DetalleServicio detalle) {
        if (detalle != null) {
            if (this.idServicio != null) {
                detalle.setIdServicio(this.idServicio);
            }
            detalles.add(detalle);
            calcularTotales();
        }
    }

    public boolean removerDetalle(DetalleServicio detalle) {
        boolean removed = detalles.remove(detalle);
        if (removed) {
            calcularTotales();
        }
        return removed;
    }

    public DetalleServicio removerDetalle(int index) {
        if (index >= 0 && index < detalles.size()) {
            DetalleServicio removed = detalles.remove(index);
            calcularTotales();
            return removed;
        }
        return null;
    }

    public void setDetalles(List<DetalleServicio> nuevosDetalles) {
        this.detalles.clear();
        if (nuevosDetalles != null) {
            for (DetalleServicio d : nuevosDetalles) {
                if (this.idServicio != null) {
                    d.setIdServicio(this.idServicio);
                }
                this.detalles.add(d);
            }
        }
        calcularTotales();
    }

    public void calcularTotales() {
        BigDecimal moAcumulado = BigDecimal.ZERO;
        BigDecimal repAcumulado = BigDecimal.ZERO;

        for (DetalleServicio d : detalles) {
            if (d == null) continue;
            BigDecimal sub = d.calcularSubtotal();
            if (d.getTipo() == TipoDetalle.MANO_DE_OBRA) {
                moAcumulado = moAcumulado.add(sub);
            } else if (d.getTipo() == TipoDetalle.REPUESTO) {
                repAcumulado = repAcumulado.add(sub);
            }
        }

        this.totalManoObra = moAcumulado.setScale(2, RoundingMode.HALF_UP);
        this.totalRepuestos = repAcumulado.setScale(2, RoundingMode.HALF_UP);
        this.total = this.totalManoObra.add(this.totalRepuestos).setScale(2, RoundingMode.HALF_UP);
    }

    // Getters y Setters
    public Long getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Long idServicio) {
        this.idServicio = idServicio;
        if (idServicio != null) {
            for (DetalleServicio d : detalles) {
                d.setIdServicio(idServicio);
            }
        }
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    // Alias para compatibilidad con código que use descripción
    public String getDescripcion() {
        return observacion;
    }

    public void setDescripcion(String descripcion) {
        this.observacion = descripcion;
    }

    public EstadoServicio getEstado() {
        return estado;
    }

    public void setEstado(EstadoServicio estado) {
        this.estado = Objects.requireNonNull(estado, "El estado del servicio no puede ser nulo");
    }

    public String getPatenteVehiculo() {
        return patenteVehiculo;
    }

    public void setPatenteVehiculo(String patenteVehiculo) {
        this.patenteVehiculo = patenteVehiculo;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(Vehiculo vehiculo) {
        this.vehiculo = vehiculo;
        if (vehiculo != null) {
            this.patenteVehiculo = vehiculo.getPatente();
        }
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public List<DetalleServicio> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public List<DetalleServicio> getDetallesModificables() {
        return detalles;
    }

    public BigDecimal getTotalManoObra() {
        if (totalManoObra == null) {
            calcularTotales();
        }
        return totalManoObra;
    }

    public void setTotalManoObra(BigDecimal totalManoObra) {
        this.totalManoObra = totalManoObra;
    }

    public BigDecimal getTotalRepuestos() {
        if (totalRepuestos == null) {
            calcularTotales();
        }
        return totalRepuestos;
    }

    public void setTotalRepuestos(BigDecimal totalRepuestos) {
        this.totalRepuestos = totalRepuestos;
    }

    public BigDecimal getTotal() {
        if (total == null) {
            calcularTotales();
        }
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Servicio{" +
                "idServicio=" + idServicio +
                ", patenteVehiculo='" + patenteVehiculo + '\'' +
                ", fecha=" + fecha +
                ", estado=" + estado +
                ", total=" + total +
                ", detalles=" + detalles.size() +
                '}';
    }
}
