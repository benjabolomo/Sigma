package com.sigma.negocio.servicios;

import com.sigma.datos.conexion.Conexion;
import com.sigma.datos.dao.DetalleServicioDAO;
import com.sigma.datos.dao.ServicioDAO;
import com.sigma.negocio.modelo.DetalleServicio;
import com.sigma.negocio.modelo.Servicio;
import com.sigma.negocio.modelo.TipoDetalle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración para el control de transacciones manual JDBC.
 * Verifica que ante el fallo de un detalle, la orden entera se revierte (rollback).
 */
class TransaccionServicioTest {

    private ServicioRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        String h2Url = "jdbc:h2:mem:sigma_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Conexion.setCustomConfig(h2Url, "sa", "");

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS vehiculo (" +
                    "patente VARCHAR(15) PRIMARY KEY, " +
                    "modelo VARCHAR(100), anio INT, kilometraje INT, id_cliente BIGINT, id_tipo INT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS servicio (" +
                    "id_servicio BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "fecha TIMESTAMP NOT NULL, " +
                    "observacion TEXT, " +
                    "estado VARCHAR(30) NOT NULL, " +
                    "patente_vehiculo VARCHAR(15) NOT NULL, " +
                    "id_usuario BIGINT NULL, " +
                    "total_mano_obra DECIMAL(12,2) DEFAULT 0.00, " +
                    "total_repuestos DECIMAL(12,2) DEFAULT 0.00, " +
                    "total DECIMAL(12,2) DEFAULT 0.00)");

            stmt.execute("CREATE TABLE IF NOT EXISTS detalle_servicio (" +
                    "id_detalle BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "id_servicio BIGINT NOT NULL, " +
                    "tipo VARCHAR(20) NOT NULL, " +
                    "descripcion VARCHAR(255) NOT NULL, " +
                    "cantidad DECIMAL(10,2) NOT NULL, " +
                    "precio_unitario DECIMAL(12,2) NOT NULL, " +
                    "subtotal DECIMAL(12,2) NOT NULL, " +
                    "CONSTRAINT fk_det_serv FOREIGN KEY (id_servicio) REFERENCES servicio(id_servicio) ON DELETE CASCADE)");

            stmt.execute("INSERT INTO vehiculo (patente, modelo, anio, kilometraje, id_cliente, id_tipo) " +
                    "VALUES ('AF123JK', 'Fiat Cronos', 2022, 25000, 1, 1)");
        }

        repository = new ServicioRepository();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
        Conexion.resetDefaultConfig();
    }

    @Test
    @DisplayName("Transacción exitosa: Guarda cabecera y detalles de manera atómica (Commit)")
    void testGuardarOrdenExitosaCommit() throws SQLException {
        Servicio servicio = new Servicio("AF123JK", "Alineación y balanceo");
        servicio.agregarDetalle(new DetalleServicio(TipoDetalle.MANO_DE_OBRA, "Alineación 3D", new BigDecimal("1.00"), new BigDecimal("12000.00")));
        servicio.agregarDetalle(new DetalleServicio(TipoDetalle.MANO_DE_OBRA, "Balanceo 4 ruedas", new BigDecimal("4.00"), new BigDecimal("3000.00")));

        repository.guardarOrdenConDetalles(servicio);

        assertNotNull(servicio.getIdServicio(), "El ID de servicio debe haber sido generado.");
        assertTrue(servicio.getIdServicio() > 0);

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rsServ = stmt.executeQuery("SELECT COUNT(*) FROM servicio WHERE id_servicio = " + servicio.getIdServicio());
            assertTrue(rsServ.next());
            assertEquals(1, rsServ.getInt(1));

            ResultSet rsDet = stmt.executeQuery("SELECT COUNT(*) FROM detalle_servicio WHERE id_servicio = " + servicio.getIdServicio());
            assertTrue(rsDet.next());
            assertEquals(2, rsDet.getInt(1));
        }
    }

    @Test
    @DisplayName("Transacción fallida: Si un detalle falla, se ejecuta ROLLBACK y la cabecera no se guarda")
    void testFalloEnDetalleEjecutaRollbackTotal() throws SQLException {
        DetalleServicioDAO detalleDaoConFalla = new DetalleServicioDAO() {
            private int llamadas = 0;
            @Override
            public void insertar(DetalleServicio detalle, Connection conn) throws SQLException {
                llamadas++;
                if (llamadas == 2) {
                    throw new SQLException("Error simulado forzado en el renglón 2");
                }
                super.insertar(detalle, conn);
            }
        };

        ServicioRepository repoConFalla = new ServicioRepository(new ServicioDAO(), detalleDaoConFalla);

        Servicio servicio = new Servicio("AF123JK", "Service Mayor");
        servicio.agregarDetalle(new DetalleServicio(TipoDetalle.MANO_DE_OBRA, "Cambio de correa", new BigDecimal("1.00"), new BigDecimal("50000.00")));
        servicio.agregarDetalle(new DetalleServicio(TipoDetalle.REPUESTO, "Kit de distribución", new BigDecimal("1.00"), new BigDecimal("65000.00")));

        assertThrows(SQLException.class, () -> repoConFalla.guardarOrdenConDetalles(servicio));

        try (Connection conn = Conexion.getConnection();
             Statement stmt = conn.createStatement()) {

            ResultSet rsServ = stmt.executeQuery("SELECT COUNT(*) FROM servicio");
            assertTrue(rsServ.next());
            assertEquals(0, rsServ.getInt(1), "No debe haber quedado guardada ninguna cabecera de servicio");

            ResultSet rsDet = stmt.executeQuery("SELECT COUNT(*) FROM detalle_servicio");
            assertTrue(rsDet.next());
            assertEquals(0, rsDet.getInt(1), "No debe haber quedado ningún detalle huérfano guardado");
        }
    }
}
