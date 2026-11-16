package com.sigma.negocio.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Entidad que representa un renglón de mano de obra o repuesto dentro de una orden de servicio.
 */
public class DetalleServicio {

    private Long idDetalle;
    private Long idServicio;
    private TipoDetalle tipo;
    private String descripcion;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public DetalleServicio() {
        this.tipo = TipoDetalle.MANO_DE_OBRA;
        this.cantidad = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
        this.precioUnitario = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public DetalleServicio(TipoDetalle tipo, String descripcion, BigDecimal cantidad, BigDecimal precioUnitario) {
        this.tipo = Objects.requireNonNull(tipo, "El tipo de detalle no puede ser nulo");
        this.descripcion = descripcion;
        this.cantidad = cantidad != null ? cantidad : BigDecimal.ONE;
        this.precioUnitario = precioUnitario != null ? precioUnitario : BigDecimal.ZERO;
        this.calcularSubtotal();
    }

    public DetalleServicio(Long idDetalle, Long idServicio, TipoDetalle tipo, String descripcion,
                           BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.idDetalle = idDetalle;
        this.idServicio = idServicio;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO;
    }

    public BigDecimal calcularSubtotal() {
        if (cantidad == null || precioUnitario == null) {
            this.subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else {
            this.subtotal = cantidad.multiply(precioUnitario).setScale(2, RoundingMode.HALF_UP);
        }
        return this.subtotal;
    }

    public boolean isManoDeObra() {
        return tipo == TipoDetalle.MANO_DE_OBRA;
    }

    public boolean isRepuesto() {
        return tipo == TipoDetalle.REPUESTO;
    }

    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Long getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Long idServicio) {
        this.idServicio = idServicio;
    }

    public TipoDetalle getTipo() {
        return tipo;
    }

    public void setTipo(TipoDetalle tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        if (subtotal == null) {
            calcularSubtotal();
        }
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "DetalleServicio{" +
                "idDetalle=" + idDetalle +
                ", idServicio=" + idServicio +
                ", tipo=" + tipo +
                ", descripcion='" + descripcion + '\'' +
                ", cantidad=" + cantidad +
                ", precioUnitario=" + precioUnitario +
                ", subtotal=" + subtotal +
                '}';
    }
}
