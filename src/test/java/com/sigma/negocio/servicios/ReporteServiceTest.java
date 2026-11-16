package com.sigma.negocio.servicios;

import com.sigma.negocio.modelo.EstadoServicio;
import com.sigma.negocio.modelo.ResultadoReporte;
import com.sigma.negocio.modelo.Servicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReporteServiceTest {
    private List<Servicio> ordenes;
    private ReporteService reporteService;

    private static Servicio orden(long id, String fecha, EstadoServicio estado, String manoObra, String repuestos) {
        BigDecimal mo = new BigDecimal(manoObra);
        BigDecimal rep = new BigDecimal(repuestos);
        return new Servicio(id, LocalDateTime.parse(fecha), "Orden " + id, estado, "AB123CD", 1L, mo, rep, mo.add(rep));
    }

    @BeforeEach
    void setUp() {
        ordenes = new ArrayList<>(Arrays.asList(
                orden(1, "2026-10-05T10:00", EstadoServicio.FINALIZADO, "20000.00", "35000.00"),
                orden(2, "2026-11-02T09:30", EstadoServicio.FINALIZADO, "15000.00", "10000.00"),
                orden(3, "2026-11-10T16:45", EstadoServicio.EN_PROCESO, "8000.00", "12000.00"),
                orden(4, "2026-11-12T11:00", EstadoServicio.CANCELADO, "5000.00", "0.00"),
                orden(5, "2026-11-14T18:00", EstadoServicio.PENDIENTE, "3000.00", "4500.00")
        ));
        reporteService = new ReporteService(() -> ordenes);
    }

    @Test
    @DisplayName("Sin filtros incluye todas las órdenes y cuenta por estado")
    void testSinFiltros() throws ServicioException {
        ResultadoReporte r = reporteService.generar(null, null, null);

        assertEquals(5, r.getCantidadOrdenes());
        assertEquals(2, r.getCantidad(EstadoServicio.FINALIZADO));
        assertEquals(1, r.getCantidad(EstadoServicio.EN_PROCESO));
        assertEquals(1, r.getCantidad(EstadoServicio.CANCELADO));
        assertEquals(1, r.getCantidad(EstadoServicio.PENDIENTE));
    }

    @Test
    @DisplayName("Los ingresos suman solo órdenes finalizadas; las canceladas no cuentan")
    void testIngresosSoloFinalizadas() throws ServicioException {
        ResultadoReporte r = reporteService.generar(null, null, null);

        assertEquals(new BigDecimal("35000.00"), r.getIngresosManoObra());
        assertEquals(new BigDecimal("45000.00"), r.getIngresosRepuestos());
        assertEquals(new BigDecimal("80000.00"), r.getIngresosTotal());

        assertEquals(new BigDecimal("27500.00"), r.getImporteEnCurso());
    }

    @Test
    @DisplayName("El filtro de fechas incluye el día completo de la fecha 'Hasta'")
    void testFiltroFechasInclusivo() throws ServicioException {
        ResultadoReporte r = reporteService.generar(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 14), null);

        assertEquals(4, r.getCantidadOrdenes());
        assertEquals(new BigDecimal("25000.00"), r.getIngresosTotal());
    }

    @Test
    @DisplayName("El filtro por estado devuelve solo ese estado")
    void testFiltroPorEstado() throws ServicioException {
        ResultadoReporte r = reporteService.generar(null, null, EstadoServicio.EN_PROCESO);

        assertEquals(1, r.getCantidadOrdenes());
        assertEquals(3L, r.getOrdenes().get(0).getIdServicio());
        assertEquals(BigDecimal.ZERO.setScale(2), r.getIngresosTotal());
    }

    @Test
    @DisplayName("Las órdenes se ordenan de la más reciente a la más antigua")
    void testOrdenDescendente() throws ServicioException {
        ResultadoReporte r = reporteService.generar(null, null, null);

        assertEquals(5L, r.getOrdenes().get(0).getIdServicio());
        assertEquals(1L, r.getOrdenes().get(4).getIdServicio());
    }

    @Test
    @DisplayName("Se rechaza un rango con 'Desde' posterior a 'Hasta'")
    void testRangoInvalido() {
        ServicioException ex = assertThrows(ServicioException.class,
                () -> reporteService.generar(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 1), null));
        assertTrue(ex.getMessage().contains("Desde"));
    }

    @Test
    @DisplayName("Un período sin órdenes devuelve un reporte vacío con totales en cero")
    void testPeriodoSinOrdenes() throws ServicioException {
        ResultadoReporte r = reporteService.generar(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), null);

        assertEquals(0, r.getCantidadOrdenes());
        assertEquals(BigDecimal.ZERO.setScale(2), r.getIngresosTotal());
        assertEquals(BigDecimal.ZERO.setScale(2), r.getImporteEnCurso());
    }
}
