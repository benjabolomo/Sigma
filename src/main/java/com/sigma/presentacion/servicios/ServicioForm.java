package com.sigma.presentacion.servicios;

import com.sigma.datos.dao.VehiculoDAO;
import com.sigma.negocio.modelo.DetalleServicio;
import com.sigma.negocio.modelo.EstadoServicio;
import com.sigma.negocio.modelo.Servicio;
import com.sigma.negocio.modelo.TipoDetalle;
import com.sigma.negocio.modelo.Vehiculo;
import com.sigma.negocio.servicios.ServicioException;
import com.sigma.negocio.servicios.ServicioService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Formulario Swing para registrar y editar órdenes de servicio con sus detalles.
 * Incluye actualización reactiva de totales financieros de mano de obra y repuestos.
 */
public class ServicioForm extends JDialog {

    private final ServicioService servicioService;
    private final VehiculoDAO vehiculoDAO;
    private final Servicio servicioEdicion;
    private final Runnable onSaveCallback;

    private JComboBox<Vehiculo> cboVehiculo;
    private JTextField txtFecha;
    private JComboBox<EstadoServicio> cboEstado;
    private JTextArea txtObservacion;

    private JTable tblDetalles;
    private DefaultTableModel tableModel;

    private JLabel lblTotalManoObra;
    private JLabel lblTotalRepuestos;
    private JLabel lblTotalGeneral;

    private final DecimalFormat currencyFormat = new DecimalFormat("$ #,##0.00");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ServicioForm(Window owner, Servicio servicio, Vehiculo vehiculoPreseleccionado, Runnable onSaveCallback) {
        super(owner, servicio == null ? "Nueva Orden de Servicio" : "Orden de Servicio N° " + servicio.getIdServicio(),
                ModalityType.APPLICATION_MODAL);
        this.servicioService = new ServicioService();
        this.vehiculoDAO = new VehiculoDAO();
        this.servicioEdicion = servicio != null ? servicio : new Servicio();
        this.onSaveCallback = onSaveCallback;

        if (vehiculoPreseleccionado != null && servicio == null) {
            this.servicioEdicion.setVehiculo(vehiculoPreseleccionado);
            this.servicioEdicion.setPatenteVehiculo(vehiculoPreseleccionado.getPatente());
        }

        inicializarComponentes();
        cargarDatos();
        actualizarEtiquetasTotales();

        setSize(850, 680);
        setLocationRelativeTo(owner);
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Panel Superior: Cabecera
        JPanel pnlCabecera = new JPanel(new GridBagLayout());
        pnlCabecera.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Datos de la Orden",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Vehículo y Fecha
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlCabecera.add(new JLabel("Vehículo (*):"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        cboVehiculo = new JComboBox<>();
        cargarVehiculosCombo();
        pnlCabecera.add(cboVehiculo, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
        pnlCabecera.add(new JLabel("Fecha:"), gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.5;
        txtFecha = new JTextField();
        txtFecha.setEditable(false);
        pnlCabecera.add(txtFecha, gbc);

        // Fila 1: Estado
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        pnlCabecera.add(new JLabel("Estado:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        cboEstado = new JComboBox<>(new EstadoServicio[]{
                EstadoServicio.PENDIENTE, EstadoServicio.EN_PROCESO,
                EstadoServicio.FINALIZADO, EstadoServicio.CANCELADO
        });
        if (servicioEdicion.getIdServicio() == null) {
            cboEstado.setSelectedItem(EstadoServicio.PENDIENTE);
            cboEstado.setEnabled(false);
        }
        pnlCabecera.add(cboEstado, gbc);

        // Fila 2: Observación / Motivo
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        pnlCabecera.add(new JLabel("Observaciones / Diagnóstico:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3; gbc.weightx = 1.0;
        txtObservacion = new JTextArea(3, 40);
        txtObservacion.setLineWrap(true);
        txtObservacion.setWrapStyleWord(true);
        pnlCabecera.add(new JScrollPane(txtObservacion), gbc);

        mainPanel.add(pnlCabecera, BorderLayout.NORTH);

        // Panel Central: Renglones de Mano de Obra y Repuestos
        JPanel pnlCentro = new JPanel(new BorderLayout(8, 8));
        pnlCentro.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Renglones de Mano de Obra y Repuestos",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 12)));

        String[] columnas = {"Tipo", "Descripción", "Cantidad", "Precio Unitario", "Subtotal"};
        tableModel = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblDetalles = new JTable(tableModel);
        tblDetalles.setRowHeight(24);
        tblDetalles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tblDetalles.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        tblDetalles.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        tblDetalles.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        pnlCentro.add(new JScrollPane(tblDetalles), BorderLayout.CENTER);

        // Botones de Renglón
        JPanel pnlBotonesDetalle = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton btnAgregarMO = new JButton("+ Mano de Obra");
        btnAgregarMO.addActionListener(e -> mostrarDialogoAgregarDetalle(TipoDetalle.MANO_DE_OBRA));

        JButton btnAgregarRepuesto = new JButton("+ Repuesto");
        btnAgregarRepuesto.addActionListener(e -> mostrarDialogoAgregarDetalle(TipoDetalle.REPUESTO));

        JButton btnEliminarRenglon = new JButton("- Eliminar Seleccionado");
        btnEliminarRenglon.addActionListener(e -> eliminarRenglonSeleccionado());

        pnlBotonesDetalle.add(btnAgregarMO);
        pnlBotonesDetalle.add(btnAgregarRepuesto);
        pnlBotonesDetalle.add(btnEliminarRenglon);

        pnlCentro.add(pnlBotonesDetalle, BorderLayout.NORTH);

        // Panel de Totales
        JPanel pnlTotales = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 6));
        pnlTotales.setBackground(new Color(245, 245, 245));
        pnlTotales.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        lblTotalManoObra = new JLabel("Mano de Obra: $ 0.00");
        lblTotalManoObra.setFont(new Font("SansSerif", Font.PLAIN, 13));

        lblTotalRepuestos = new JLabel("Repuestos: $ 0.00");
        lblTotalRepuestos.setFont(new Font("SansSerif", Font.PLAIN, 13));

        lblTotalGeneral = new JLabel("TOTAL: $ 0.00");
        lblTotalGeneral.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblTotalGeneral.setForeground(new Color(0, 102, 0));

        pnlTotales.add(lblTotalManoObra);
        pnlTotales.add(new JSeparator(SwingConstants.VERTICAL));
        pnlTotales.add(lblTotalRepuestos);
        pnlTotales.add(new JSeparator(SwingConstants.VERTICAL));
        pnlTotales.add(lblTotalGeneral);

        pnlCentro.add(pnlTotales, BorderLayout.SOUTH);
        mainPanel.add(pnlCentro, BorderLayout.CENTER);

        // Panel Inferior: Guardar / Cancelar
        JPanel pnlAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton btnGuardar = new JButton("Guardar Orden");
        btnGuardar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnGuardar.setBackground(new Color(0, 120, 215));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> guardarOrden());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        pnlAcciones.add(btnGuardar);
        pnlAcciones.add(btnCancelar);
        mainPanel.add(pnlAcciones, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void cargarVehiculosCombo() {
        cboVehiculo.removeAllItems();
        try {
            List<Vehiculo> vehiculos = vehiculoDAO.listarTodos();
            for (Vehiculo v : vehiculos) {
                cboVehiculo.addItem(v);
            }
        } catch (Exception ex) {
            System.err.println("Advertencia al cargar vehículos: " + ex.getMessage());
        }
    }

    private void cargarDatos() {
        if (servicioEdicion.getFecha() != null) {
            txtFecha.setText(servicioEdicion.getFecha().format(dateFormatter));
        } else {
            txtFecha.setText(java.time.LocalDateTime.now().format(dateFormatter));
        }

        if (servicioEdicion.getEstado() != null) {
            cboEstado.setSelectedItem(servicioEdicion.getEstado());
        }

        if (servicioEdicion.getObservacion() != null) {
            txtObservacion.setText(servicioEdicion.getObservacion());
        }

        if (servicioEdicion.getPatenteVehiculo() != null) {
            for (int i = 0; i < cboVehiculo.getItemCount(); i++) {
                Vehiculo v = cboVehiculo.getItemAt(i);
                if (v != null && v.getPatente().equalsIgnoreCase(servicioEdicion.getPatenteVehiculo())) {
                    cboVehiculo.setSelectedIndex(i);
                    break;
                }
            }
        }

        refrescarTablaDetalles();
    }

    private void refrescarTablaDetalles() {
        tableModel.setRowCount(0);
        for (DetalleServicio d : servicioEdicion.getDetalles()) {
            tableModel.addRow(new Object[]{
                    d.getTipo().getDescripcion(),
                    d.getDescripcion(),
                    d.getCantidad().toString(),
                    currencyFormat.format(d.getPrecioUnitario()),
                    currencyFormat.format(d.getSubtotal())
            });
        }
        actualizarEtiquetasTotales();
    }

    private void actualizarEtiquetasTotales() {
        servicioEdicion.calcularTotales();
        lblTotalManoObra.setText("Mano de Obra: " + currencyFormat.format(servicioEdicion.getTotalManoObra()));
        lblTotalRepuestos.setText("Repuestos: " + currencyFormat.format(servicioEdicion.getTotalRepuestos()));
        lblTotalGeneral.setText("TOTAL: " + currencyFormat.format(servicioEdicion.getTotal()));
    }

    private void mostrarDialogoAgregarDetalle(TipoDetalle tipo) {
        JTextField txtDesc = new JTextField();
        JTextField txtCant = new JTextField("1.00");
        JTextField txtPrecio = new JTextField("0.00");

        JPanel pnl = new JPanel(new GridLayout(3, 2, 8, 8));
        pnl.add(new JLabel("Descripción del ítem:"));
        pnl.add(txtDesc);
        pnl.add(new JLabel("Cantidad:"));
        pnl.add(txtCant);
        pnl.add(new JLabel("Precio Unitario ($):"));
        pnl.add(txtPrecio);

        int result = JOptionPane.showConfirmDialog(
                this, pnl, "Agregar " + tipo.getDescripcion(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String desc = txtDesc.getText().trim();
            if (desc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "La descripción es obligatoria.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BigDecimal cant;
            BigDecimal precio;
            try {
                cant = new BigDecimal(txtCant.getText().trim().replace(",", "."));
                if (cant.compareTo(BigDecimal.ZERO) <= 0) {
                    JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a cero.", "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Cantidad numérica inválida.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                precio = new BigDecimal(txtPrecio.getText().trim().replace(",", "."));
                if (precio.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(this, "El precio unitario no puede ser negativo.", "Validación", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Precio unitario inválido.", "Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            DetalleServicio nuevo = new DetalleServicio(tipo, desc, cant, precio);
            servicioEdicion.agregarDetalle(nuevo);
            refrescarTablaDetalles();
        }
    }

    private void eliminarRenglonSeleccionado() {
        int fila = tblDetalles.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un renglón para eliminar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        servicioEdicion.removerDetalle(fila);
        refrescarTablaDetalles();
    }

    private void guardarOrden() {
        Vehiculo vSeleccionado = (Vehiculo) cboVehiculo.getSelectedItem();
        if (vSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un vehículo.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String obs = txtObservacion.getText().trim();
        if (obs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar las observaciones / motivo de ingreso.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        servicioEdicion.setVehiculo(vSeleccionado);
        servicioEdicion.setPatenteVehiculo(vSeleccionado.getPatente());
        servicioEdicion.setObservacion(obs);
        if (cboEstado.isEnabled() && cboEstado.getSelectedItem() != null) {
            servicioEdicion.setEstado((EstadoServicio) cboEstado.getSelectedItem());
        }

        try {
            if (servicioEdicion.getIdServicio() == null || servicioEdicion.getIdServicio() == 0) {
                servicioService.registrarOrden(servicioEdicion);
                JOptionPane.showMessageDialog(this,
                        "Orden de servicio N° " + servicioEdicion.getIdServicio() + " registrada con éxito.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                servicioService.actualizarOrden(servicioEdicion);
                JOptionPane.showMessageDialog(this,
                        "Orden de servicio N° " + servicioEdicion.getIdServicio() + " actualizada con éxito.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            dispose();

        } catch (ServicioException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar la orden de servicio:\n" + ex.getMessage(),
                    "Error de Negocio / Transacción Revertida", JOptionPane.ERROR_MESSAGE);
        }
    }
}
