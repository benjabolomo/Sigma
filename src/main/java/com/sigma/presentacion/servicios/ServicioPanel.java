package com.sigma.presentacion.servicios;

import com.sigma.datos.dao.VehiculoDAO;
import com.sigma.negocio.modelo.EstadoServicio;
import com.sigma.negocio.modelo.Servicio;
import com.sigma.negocio.modelo.Vehiculo;
import com.sigma.negocio.servicios.ServicioException;
import com.sigma.negocio.servicios.ServicioService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel principal en Swing para la gestión de órdenes de servicio y consulta de historial.
 * Cumple con los requerimientos de arquitectura de tres capas y presentación visual.
 */
public class ServicioPanel extends JPanel {

    private final ServicioService servicioService;
    private final VehiculoDAO vehiculoDAO;

    private JComboBox<VehiculoItem> cboFiltroVehiculo;
    private JTextField txtBuscarPatente;
    private JTable tblServicios;
    private DefaultTableModel tableModel;

    private JLabel lblResumenRegistros;
    private List<Servicio> listaServiciosActuales;

    private final DecimalFormat currencyFormat = new DecimalFormat("$ #,##0.00");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ServicioPanel() {
        this.servicioService = new ServicioService();
        this.vehiculoDAO = new VehiculoDAO();

        inicializarComponentes();
        cargarComboVehiculos();
        cargarHistorialCompleto();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // 1. Filtros
        JPanel pnlFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlFiltros.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Consulta de Historial y Filtros por Vehículo",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));

        pnlFiltros.add(new JLabel("Seleccionar Vehículo:"));
        cboFiltroVehiculo = new JComboBox<>();
        cboFiltroVehiculo.setPreferredSize(new Dimension(320, 26));
        cboFiltroVehiculo.addActionListener(e -> filtrarPorVehiculoSeleccionado());
        pnlFiltros.add(cboFiltroVehiculo);

        pnlFiltros.add(Box.createHorizontalStrut(10));
        pnlFiltros.add(new JLabel("O buscar por Patente:"));
        txtBuscarPatente = new JTextField(9);
        pnlFiltros.add(txtBuscarPatente);

        JButton btnBuscarPatente = new JButton("Buscar");
        btnBuscarPatente.addActionListener(e -> buscarPorPatente());
        pnlFiltros.add(btnBuscarPatente);

        JButton btnLimpiar = new JButton("Limpiar Filtros");
        btnLimpiar.addActionListener(e -> {
            txtBuscarPatente.setText("");
            if (cboFiltroVehiculo.getItemCount() > 0) {
                cboFiltroVehiculo.setSelectedIndex(0);
            }
            cargarHistorialCompleto();
        });
        pnlFiltros.add(btnLimpiar);

        add(pnlFiltros, BorderLayout.NORTH);

        // 2. Tabla Central
        JPanel pnlCentro = new JPanel(new BorderLayout(5, 5));
        pnlCentro.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Historial de Órdenes de Servicio",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));

        String[] columnas = {
                "ID", "Fecha", "Patente", "Vehículo", "Cliente", "Estado",
                "Mano de Obra", "Repuestos", "Total", "Observaciones"
        };

        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblServicios = new JTable(tableModel);
        tblServicios.setRowHeight(24);
        tblServicios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblServicios.getTableHeader().setReorderingAllowed(false);

        // Anchos de columna
        tblServicios.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblServicios.getColumnModel().getColumn(1).setPreferredWidth(115);
        tblServicios.getColumnModel().getColumn(2).setPreferredWidth(85);
        tblServicios.getColumnModel().getColumn(3).setPreferredWidth(140);
        tblServicios.getColumnModel().getColumn(4).setPreferredWidth(140);
        tblServicios.getColumnModel().getColumn(5).setPreferredWidth(95);
        tblServicios.getColumnModel().getColumn(6).setPreferredWidth(95);
        tblServicios.getColumnModel().getColumn(7).setPreferredWidth(95);
        tblServicios.getColumnModel().getColumn(8).setPreferredWidth(100);
        tblServicios.getColumnModel().getColumn(9).setPreferredWidth(200);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblServicios.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        tblServicios.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        tblServicios.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);

        tblServicios.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected && value != null) {
                    String str = value.toString();
                    if (str.equalsIgnoreCase(EstadoServicio.PENDIENTE.getNombreVisible()) || str.equalsIgnoreCase("Borrador")) {
                        c.setForeground(Color.DARK_GRAY);
                    } else if (str.equalsIgnoreCase(EstadoServicio.EN_PROCESO.getNombreVisible())) {
                        c.setForeground(new Color(204, 102, 0));
                    } else if (str.equalsIgnoreCase(EstadoServicio.FINALIZADO.getNombreVisible())) {
                        c.setForeground(new Color(0, 153, 51));
                    } else if (str.equalsIgnoreCase(EstadoServicio.CANCELADO.getNombreVisible())) {
                        c.setForeground(Color.RED);
                    }
                }
                return c;
            }
        });

        pnlCentro.add(new JScrollPane(tblServicios), BorderLayout.CENTER);

        lblResumenRegistros = new JLabel("Registros cargados: 0");
        lblResumenRegistros.setBorder(new EmptyBorder(4, 4, 4, 4));
        lblResumenRegistros.setFont(new Font("SansSerif", Font.ITALIC, 11));
        pnlCentro.add(lblResumenRegistros, BorderLayout.SOUTH);

        add(pnlCentro, BorderLayout.CENTER);

        // 3. Botones de Acción
        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));

        JButton btnNuevaOrden = new JButton("Nueva Orden de Servicio");
        btnNuevaOrden.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnNuevaOrden.setBackground(new Color(0, 120, 215));
        btnNuevaOrden.setForeground(Color.WHITE);
        btnNuevaOrden.setFocusPainted(false);
        btnNuevaOrden.addActionListener(e -> abrirNuevaOrden());

        JButton btnVerEditar = new JButton("Ver / Editar Detalle");
        btnVerEditar.addActionListener(e -> abrirEditarOrden());

        JButton btnCambiarEstado = new JButton("Cambiar Estado...");
        btnCambiarEstado.addActionListener(e -> cambiarEstadoSeleccionado());

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> refrescarVista());

        pnlAcciones.add(btnNuevaOrden);
        pnlAcciones.add(btnVerEditar);
        pnlAcciones.add(btnCambiarEstado);
        pnlAcciones.add(btnRefrescar);

        add(pnlAcciones, BorderLayout.SOUTH);
    }

    private void cargarComboVehiculos() {
        cboFiltroVehiculo.removeAllItems();
        cboFiltroVehiculo.addItem(new VehiculoItem(null, "-- Todos los Vehículos --"));
        try {
            List<Vehiculo> vehiculos = vehiculoDAO.listarTodos();
            for (Vehiculo v : vehiculos) {
                cboFiltroVehiculo.addItem(new VehiculoItem(v, v.toString()));
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar combo de vehículos: " + ex.getMessage());
        }
    }

    private void cargarHistorialCompleto() {
        try {
            listaServiciosActuales = servicioService.listarTodas();
            actualizarTabla(listaServiciosActuales);
        } catch (ServicioException ex) {
            System.err.println("Aviso al consultar servicios: " + ex.getMessage());
        }
    }

    private void filtrarPorVehiculoSeleccionado() {
        VehiculoItem item = (VehiculoItem) cboFiltroVehiculo.getSelectedItem();
        if (item == null || item.getVehiculo() == null) {
            cargarHistorialCompleto();
            return;
        }

        try {
            listaServiciosActuales = servicioService.consultarHistorialPorPatente(item.getVehiculo().getPatente());
            actualizarTabla(listaServiciosActuales);
        } catch (ServicioException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al filtrar por vehículo:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarPorPatente() {
        String patente = txtBuscarPatente.getText().trim();
        if (patente.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese una patente para buscar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            listaServiciosActuales = servicioService.consultarHistorialPorPatente(patente);
            actualizarTabla(listaServiciosActuales);

            for (int i = 1; i < cboFiltroVehiculo.getItemCount(); i++) {
                VehiculoItem vi = cboFiltroVehiculo.getItemAt(i);
                if (vi.getVehiculo() != null && vi.getVehiculo().getPatente().equalsIgnoreCase(patente)) {
                    cboFiltroVehiculo.setSelectedIndex(i);
                    break;
                }
            }

        } catch (ServicioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Búsqueda por Patente", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarTabla(List<Servicio> servicios) {
        tableModel.setRowCount(0);
        if (servicios != null) {
            for (Servicio s : servicios) {
                Vehiculo v = s.getVehiculo();
                String patente = s.getPatenteVehiculo() != null ? s.getPatenteVehiculo() : "-";
                String modeloDesc = (v != null && v.getModelo() != null) ? v.getModelo() : "-";
                String clienteDesc = (v != null && v.getNombreCliente() != null) ? v.getNombreCliente() : "-";

                tableModel.addRow(new Object[]{
                        s.getIdServicio(),
                        s.getFecha() != null ? s.getFecha().format(dateFormatter) : "-",
                        patente,
                        modeloDesc,
                        clienteDesc,
                        s.getEstado().getNombreVisible(),
                        currencyFormat.format(s.getTotalManoObra()),
                        currencyFormat.format(s.getTotalRepuestos()),
                        currencyFormat.format(s.getTotal()),
                        s.getObservacion() != null ? s.getObservacion() : ""
                });
            }
            lblResumenRegistros.setText("Órdenes listadas: " + servicios.size());
        } else {
            lblResumenRegistros.setText("Órdenes listadas: 0");
        }
    }

    private void abrirNuevaOrden() {
        Vehiculo preseleccionado = null;
        VehiculoItem item = (VehiculoItem) cboFiltroVehiculo.getSelectedItem();
        if (item != null && item.getVehiculo() != null) {
            preseleccionado = item.getVehiculo();
        }

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        ServicioForm form = new ServicioForm(parentWindow, null, preseleccionado, this::refrescarVista);
        form.setVisible(true);
    }

    private void abrirEditarOrden() {
        int fila = tblServicios.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden para ver o editar.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Long idServicio = (Long) tableModel.getValueAt(fila, 0);
        try {
            Servicio servicioCompleto = servicioService.obtenerPorId(idServicio);
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            ServicioForm form = new ServicioForm(parentWindow, servicioCompleto, null, this::refrescarVista);
            form.setVisible(true);
        } catch (ServicioException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo cargar la orden:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarEstadoSeleccionado() {
        int fila = tblServicios.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden para cambiar su estado.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Long idServicio = (Long) tableModel.getValueAt(fila, 0);
        Servicio s;
        try {
            s = servicioService.obtenerPorId(idServicio);
        } catch (ServicioException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        EstadoServicio[] posibles = new EstadoServicio[]{
                EstadoServicio.PENDIENTE, EstadoServicio.EN_PROCESO,
                EstadoServicio.FINALIZADO, EstadoServicio.CANCELADO
        };

        EstadoServicio seleccionado = (EstadoServicio) JOptionPane.showInputDialog(
                this,
                "Estado actual: " + s.getEstado().getNombreVisible() + "\nSeleccione el nuevo estado:",
                "Gestionar Cambio de Estado",
                JOptionPane.QUESTION_MESSAGE,
                null,
                posibles,
                s.getEstado()
        );

        if (seleccionado != null && seleccionado != s.getEstado()) {
            try {
                servicioService.cambiarEstado(idServicio, seleccionado);
                JOptionPane.showMessageDialog(this,
                        "Estado cambiado con éxito a '" + seleccionado.getNombreVisible() + "'.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                refrescarVista();
            } catch (ServicioException ex) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo realizar la transición de estado:\n" + ex.getMessage(),
                        "Regla de Negocio", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void refrescarVista() {
        VehiculoItem vi = (VehiculoItem) cboFiltroVehiculo.getSelectedItem();
        if (vi != null && vi.getVehiculo() != null) {
            filtrarPorVehiculoSeleccionado();
        } else {
            cargarHistorialCompleto();
        }
    }

    private static class VehiculoItem {
        private final Vehiculo vehiculo;
        private final String label;

        public VehiculoItem(Vehiculo vehiculo, String label) {
            this.vehiculo = vehiculo;
            this.label = label;
        }

        public Vehiculo getVehiculo() {
            return vehiculo;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
