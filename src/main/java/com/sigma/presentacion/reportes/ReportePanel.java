package com.sigma.presentacion.reportes;

import com.sigma.negocio.modelo.EstadoServicio;
import com.sigma.negocio.modelo.ResultadoReporte;
import com.sigma.negocio.modelo.Servicio;
import com.sigma.negocio.modelo.Vehiculo;
import com.sigma.negocio.servicios.ReporteService;
import com.sigma.negocio.servicios.ServicioException;
import com.sigma.presentacion.componentes.DialogoConfirmacion;
import com.sigma.presentacion.componentes.Estilos;
import com.sigma.presentacion.componentes.TablaPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReportePanel extends JPanel {
    private static final String TODOS = "Todos los estados";

    private final ReporteService reporteService;

    private JSpinner spnDesde;
    private JSpinner spnHasta;
    private JComboBox<Object> cboEstado;

    private JLabel lblCantidadOrdenes;
    private JLabel lblFinalizadas;
    private JLabel lblManoObra;
    private JLabel lblRepuestos;
    private JLabel lblIngresos;
    private JLabel lblEnCurso;
    private JLabel lblPorEstado;

    private TablaPanel tabla;

    public ReportePanel() {
        this(new ReporteService());
    }

    public ReportePanel(ReporteService reporteService) {
        this.reporteService = reporteService;
        inicializarComponentes();
        restablecerFiltros();
        generarReporte();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(Estilos.MARGEN, Estilos.MARGEN, Estilos.MARGEN, Estilos.MARGEN));

        JPanel superior = new JPanel(new BorderLayout(10, 10));
        superior.add(crearPanelFiltros(), BorderLayout.NORTH);
        superior.add(crearPanelResumen(), BorderLayout.CENTER);
        add(superior, BorderLayout.NORTH);

        tabla = new TablaPanel("Órdenes incluidas en el reporte",
                "N°", "Fecha", "Patente", "Vehículo", "Cliente", "Estado", "Mano de Obra", "Repuestos", "Total");
        tabla.setAnchos(45, 115, 80, 140, 150, 95, 100, 100, 105);
        tabla.setColumnasCentradas(0, 2);
        tabla.setColumnaEstado(5);
        tabla.setColumnasDerecha(6, 7, 8);
        add(tabla, BorderLayout.CENTER);
    }

    private JPanel crearPanelFiltros() {
        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        pnl.setBorder(Estilos.bordeSeccion("Filtros del reporte"));

        spnDesde = crearSelectorFecha();
        spnHasta = crearSelectorFecha();

        cboEstado = new JComboBox<>();
        cboEstado.addItem(TODOS);
        for (EstadoServicio e : EstadoServicio.values()) {
            cboEstado.addItem(e);
        }

        JButton btnGenerar = Estilos.botonPrimario("Generar reporte");
        btnGenerar.addActionListener(e -> generarReporte());

        JButton btnMesActual = Estilos.botonSecundario("Mes actual");
        btnMesActual.addActionListener(e -> {
            restablecerFiltros();
            generarReporte();
        });

        pnl.add(new JLabel("Desde:"));
        pnl.add(spnDesde);
        pnl.add(new JLabel("Hasta:"));
        pnl.add(spnHasta);
        pnl.add(new JLabel("Estado:"));
        pnl.add(cboEstado);
        pnl.add(btnGenerar);
        pnl.add(btnMesActual);
        return pnl;
    }

    private JSpinner crearSelectorFecha() {
        JSpinner spn = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spn.setEditor(new JSpinner.DateEditor(spn, "dd/MM/yyyy"));
        return spn;
    }

    // Filtros por defecto: mes actual y todos los estados.
    private void restablecerFiltros() {
        LocalDate hoy = LocalDate.now();
        spnDesde.setValue(aDate(hoy.withDayOfMonth(1)));
        spnHasta.setValue(aDate(hoy));
        cboEstado.setSelectedIndex(0);
    }

    private JPanel crearPanelResumen() {
        JPanel pnl = new JPanel(new BorderLayout(5, 5));
        pnl.setBorder(Estilos.bordeSeccion("Resumen"));

        JPanel tarjetas = new JPanel(new GridLayout(1, 6, 8, 8));
        lblCantidadOrdenes = new JLabel();
        lblFinalizadas = new JLabel();
        lblManoObra = new JLabel();
        lblRepuestos = new JLabel();
        lblIngresos = new JLabel();
        lblEnCurso = new JLabel();

        tarjetas.add(crearTarjeta("Órdenes", lblCantidadOrdenes, Estilos.NEUTRO));
        tarjetas.add(crearTarjeta("Finalizadas", lblFinalizadas, Estilos.EXITO));
        tarjetas.add(crearTarjeta("Mano de obra cobrada", lblManoObra, Estilos.NEUTRO));
        tarjetas.add(crearTarjeta("Repuestos cobrados", lblRepuestos, Estilos.NEUTRO));
        tarjetas.add(crearTarjeta("Ingresos totales", lblIngresos, Estilos.EXITO));
        tarjetas.add(crearTarjeta("Importe en curso", lblEnCurso, Estilos.ADVERTENCIA));

        lblPorEstado = new JLabel();
        lblPorEstado.setFont(Estilos.NORMAL);
        lblPorEstado.setBorder(new EmptyBorder(2, 4, 2, 4));

        pnl.add(tarjetas, BorderLayout.CENTER);
        pnl.add(lblPorEstado, BorderLayout.SOUTH);
        return pnl;
    }

    private JPanel crearTarjeta(String titulo, JLabel lblValor, Color colorValor) {
        JPanel tarjeta = new JPanel(new BorderLayout(2, 2));
        tarjeta.setBackground(Estilos.FONDO_SUAVE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                new EmptyBorder(8, 10, 8, 10)));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Estilos.PEQUENA);
        lblValor.setFont(Estilos.DESTACADA);
        lblValor.setForeground(colorValor);

        tarjeta.add(lblTitulo, BorderLayout.NORTH);
        tarjeta.add(lblValor, BorderLayout.CENTER);
        return tarjeta;
    }

    public void generarReporte() {
        LocalDate desde = aLocalDate((Date) spnDesde.getValue());
        LocalDate hasta = aLocalDate((Date) spnHasta.getValue());
        Object seleccion = cboEstado.getSelectedItem();
        EstadoServicio estado = seleccion instanceof EstadoServicio ? (EstadoServicio) seleccion : null;

        try {
            mostrar(reporteService.generar(desde, hasta, estado));
        } catch (ServicioException ex) {
            DialogoConfirmacion.mostrarAviso(this, ex.getMessage());
        } catch (RuntimeException ex) {
            DialogoConfirmacion.mostrarError(this, "No se pudo generar el reporte:\n" + ex.getMessage());
        }
    }

    private void mostrar(ResultadoReporte r) {
        lblCantidadOrdenes.setText(String.valueOf(r.getCantidadOrdenes()));
        lblFinalizadas.setText(String.valueOf(r.getCantidad(EstadoServicio.FINALIZADO)));
        lblManoObra.setText(Estilos.moneda(r.getIngresosManoObra()));
        lblRepuestos.setText(Estilos.moneda(r.getIngresosRepuestos()));
        lblIngresos.setText(Estilos.moneda(r.getIngresosTotal()));
        lblEnCurso.setText(Estilos.moneda(r.getImporteEnCurso()));

        StringBuilder porEstado = new StringBuilder("Órdenes por estado:   ");
        for (EstadoServicio e : EstadoServicio.values()) {
            int cantidad = r.getCantidad(e);
            if (cantidad > 0) {
                porEstado.append(e.getNombreVisible()).append(": ").append(cantidad).append("    ");
            }
        }
        if (r.getCantidadOrdenes() == 0) {
            porEstado.append("no hay órdenes para los filtros seleccionados.");
        }
        lblPorEstado.setText(porEstado.toString());

        List<Object[]> filas = new ArrayList<>();
        for (Servicio s : r.getOrdenes()) {
            Vehiculo v = s.getVehiculo();
            filas.add(new Object[]{
                    s.getIdServicio(),
                    s.getFecha() != null ? s.getFecha().format(Estilos.FORMATO_FECHA_HORA) : "-",
                    s.getPatenteVehiculo() != null ? s.getPatenteVehiculo() : "-",
                    v != null && v.getModelo() != null ? v.getModelo() : "-",
                    v != null && v.getNombreCliente() != null ? v.getNombreCliente() : "-",
                    s.getEstado().getNombreVisible(),
                    Estilos.moneda(s.getTotalManoObra()),
                    Estilos.moneda(s.getTotalRepuestos()),
                    Estilos.moneda(s.getTotal())
            });
        }
        tabla.setFilas(filas);
    }

    private static LocalDate aLocalDate(Date fecha) {
        return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date aDate(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
