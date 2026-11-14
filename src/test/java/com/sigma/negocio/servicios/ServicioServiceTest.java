package com.sigma.negocio.servicios;

import com.sigma.negocio.modelo.DetalleServicio;
import com.sigma.negocio.modelo.EstadoServicio;
import com.sigma.negocio.modelo.Servicio;
import com.sigma.negocio.modelo.TipoDetalle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServicioServiceTest {

    private ServicioService servicioService;

    @BeforeEach
    void setUp() {
        servicioService = new ServicioService();
    }

    @Test
    @DisplayName("Debe calcular correctamente subtotales de mano de obra, repuestos y el total general")
    void testCalculoTotalesServicio() {
        Servicio servicio = new Servicio();

        DetalleServicio mo1 = new DetalleServicio(
                TipoDetalle.MANO_DE_OBRA,
                "Service y cambio de aceite y filtros",
                new BigDecimal("1.00"),
                new BigDecimal("35000.00")
        );

        DetalleServicio rep1 = new DetalleServicio(
                TipoDetalle.REPUESTO,
                "Aceite sintético 5W30 (bidón)",
                new BigDecimal("2.00"),
                new BigDecimal("17500.00")
        );

        DetalleServicio rep2 = new DetalleServicio(
                TipoDetalle.REPUESTO,
                "Filtro de aceite original",
                new BigDecimal("1.00"),
                new BigDecimal("8500.50")
        );

        servicio.agregarDetalle(mo1);
        servicio.agregarDetalle(rep1);
        servicio.agregarDetalle(rep2);

        servicioService.calcularTotales(servicio);

        assertEquals(new BigDecimal("35000.00"), servicio.getTotalManoObra());
        assertEquals(new BigDecimal("43500.50"), servicio.getTotalRepuestos());
        assertEquals(new BigDecimal("78500.50"), servicio.getTotal());
    }

    @Test
    @DisplayName("Debe validar transiciones de estado permitidas en el ciclo de vida")
    void testTransicionesValidas() {
        assertTrue(EstadoServicio.PENDIENTE.esTransicionValida(EstadoServicio.EN_PROCESO));
        assertTrue(EstadoServicio.PENDIENTE.esTransicionValida(EstadoServicio.CANCELADO));

        assertTrue(EstadoServicio.EN_PROCESO.esTransicionValida(EstadoServicio.FINALIZADO));
        assertTrue(EstadoServicio.EN_PROCESO.esTransicionValida(EstadoServicio.CANCELADO));
        assertFalse(EstadoServicio.EN_PROCESO.esTransicionValida(EstadoServicio.PENDIENTE));

        assertFalse(EstadoServicio.FINALIZADO.esTransicionValida(EstadoServicio.EN_PROCESO));
        assertFalse(EstadoServicio.CANCELADO.esTransicionValida(EstadoServicio.PENDIENTE));
    }

    @Test
    @DisplayName("Debe rechazar el registro de una orden sin patente asociada")
    void testRechazoOrdenSinVehiculo() {
        Servicio orden = new Servicio();
        orden.setPatenteVehiculo(null);
        orden.setObservacion("Revisión general de frenos");

        assertThrows(ServicioException.class, () -> servicioService.registrarOrden(orden));
    }

    @Test
    @DisplayName("Debe rechazar renglones con cantidad menor o igual a cero")
    void testRechazoDetalleCantidadInvalida() {
        Servicio orden = new Servicio("AF123JK", "Service 10.000 km");
        DetalleServicio d = new DetalleServicio(TipoDetalle.MANO_DE_OBRA, "Alineación", new BigDecimal("0.00"), new BigDecimal("15000.00"));
        orden.agregarDetalle(d);

        assertThrows(ServicioException.class, () -> servicioService.registrarOrden(orden));
    }
}
