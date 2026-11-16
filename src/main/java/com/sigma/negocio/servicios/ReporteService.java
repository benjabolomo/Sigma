package com.sigma.negocio.servicios;

import com.sigma.negocio.modelo.EstadoServicio;
import com.sigma.negocio.modelo.ResultadoReporte;
import com.sigma.negocio.modelo.Servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ReporteService {
    @FunctionalInterface
    public interface FuenteOrdenes {
        List<Servicio> obtenerOrdenes() throws ServicioException;
    }

    private final FuenteOrdenes fuente;

    public ReporteService() {
        this(new ServicioService()::listarTodas);
    }

    public ReporteService(FuenteOrdenes fuente) {
        if (fuente == null) {
            throw new IllegalArgumentException("La fuente de órdenes no puede ser nula.");
        }
        this.fuente = fuente;
    }

    public ResultadoReporte generar(LocalDate desde, LocalDate hasta, EstadoServicio estado) throws ServicioException {
        validarRango(desde, hasta);

        List<Servicio> todas = fuente.obtenerOrdenes();
        List<Servicio> filtradas = new ArrayList<>();
        if (todas != null) {
            for (Servicio s : todas) {
                if (s != null && estaEnRango(s.getFecha(), desde, hasta) && coincideEstado(s, estado)) {
                    filtradas.add(s);
                }
            }
        }
        filtradas.sort(Comparator.comparing(Servicio::getFecha,
                Comparator.nullsLast(Comparator.reverseOrder())));

        Map<EstadoServicio, Integer> porEstado = new EnumMap<>(EstadoServicio.class);
        BigDecimal manoObra = BigDecimal.ZERO;
        BigDecimal repuestos = BigDecimal.ZERO;
        BigDecimal enCurso = BigDecimal.ZERO;

        // Solo las órdenes finalizadas cuentan como ingreso; las canceladas no suman.
        for (Servicio s : filtradas) {
            EstadoServicio e = s.getEstado();
            porEstado.merge(e, 1, Integer::sum);

            if (e == EstadoServicio.FINALIZADO) {
                manoObra = manoObra.add(valor(s.getTotalManoObra()));
                repuestos = repuestos.add(valor(s.getTotalRepuestos()));
            } else if (e != EstadoServicio.CANCELADO) {
                enCurso = enCurso.add(valor(s.getTotal()));
            }
        }

        return new ResultadoReporte(desde, hasta, estado, filtradas, porEstado, manoObra, repuestos, enCurso);
    }

    public void validarRango(LocalDate desde, LocalDate hasta) throws ServicioException {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ServicioException("La fecha \"Desde\" no puede ser posterior a la fecha \"Hasta\".");
        }
    }

    private static boolean estaEnRango(LocalDateTime fecha, LocalDate desde, LocalDate hasta) {
        if (fecha == null) {
            return desde == null && hasta == null;
        }
        LocalDate dia = fecha.toLocalDate();
        return (desde == null || !dia.isBefore(desde)) && (hasta == null || !dia.isAfter(hasta));
    }

    private static boolean coincideEstado(Servicio s, EstadoServicio estado) {
        return estado == null || s.getEstado() == estado;
    }

    private static BigDecimal valor(BigDecimal importe) {
        return importe != null ? importe : BigDecimal.ZERO;
    }
}
