package com.sigma.negocio.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ResultadoReporte {
    private final LocalDate desde;
    private final LocalDate hasta;
    private final EstadoServicio estadoFiltro;
    private final List<Servicio> ordenes;
    private final Map<EstadoServicio, Integer> cantidadPorEstado;
    private final BigDecimal ingresosManoObra;
    private final BigDecimal ingresosRepuestos;
    private final BigDecimal ingresosTotal;
    private final BigDecimal importeEnCurso;

    public ResultadoReporte(LocalDate desde, LocalDate hasta, EstadoServicio estadoFiltro,
                            List<Servicio> ordenes, Map<EstadoServicio, Integer> cantidadPorEstado,
                            BigDecimal ingresosManoObra, BigDecimal ingresosRepuestos,
                            BigDecimal importeEnCurso) {
        this.desde = desde;
        this.hasta = hasta;
        this.estadoFiltro = estadoFiltro;
        this.ordenes = Collections.unmodifiableList(ordenes);
        this.cantidadPorEstado = Collections.unmodifiableMap(new EnumMap<>(cantidadPorEstado));
        this.ingresosManoObra = escala(ingresosManoObra);
        this.ingresosRepuestos = escala(ingresosRepuestos);
        this.ingresosTotal = escala(this.ingresosManoObra.add(this.ingresosRepuestos));
        this.importeEnCurso = escala(importeEnCurso);
    }

    private static BigDecimal escala(BigDecimal valor) {
        return (valor != null ? valor : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public LocalDate getDesde() {
        return desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public EstadoServicio getEstadoFiltro() {
        return estadoFiltro;
    }

    public List<Servicio> getOrdenes() {
        return ordenes;
    }

    public int getCantidadOrdenes() {
        return ordenes.size();
    }

    public int getCantidad(EstadoServicio estado) {
        return cantidadPorEstado.getOrDefault(estado, 0);
    }

    public Map<EstadoServicio, Integer> getCantidadPorEstado() {
        return cantidadPorEstado;
    }

    public BigDecimal getIngresosManoObra() {
        return ingresosManoObra;
    }

    public BigDecimal getIngresosRepuestos() {
        return ingresosRepuestos;
    }

    public BigDecimal getIngresosTotal() {
        return ingresosTotal;
    }

    public BigDecimal getImporteEnCurso() {
        return importeEnCurso;
    }
}
