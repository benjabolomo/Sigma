package com.sigma.presentacion.componentes;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.IntConsumer;

public class TablaPanel extends JPanel {
    private final DefaultTableModel modelo;
    private final JTable tabla;
    private final JLabel lblRegistros;

    public TablaPanel(String titulo, String... columnas) {
        super(new BorderLayout(5, 5));
        if (titulo != null) {
            setBorder(Estilos.bordeSeccion(titulo));
        }

        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(Estilos.ALTO_FILA);
        tabla.setFont(Estilos.NORMAL);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setFillsViewportHeight(true);

        lblRegistros = new JLabel();
        lblRegistros.setFont(Estilos.PEQUENA);
        lblRegistros.setBorder(new EmptyBorder(4, 4, 4, 4));

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblRegistros, BorderLayout.SOUTH);
        actualizarContador();
    }

    public void agregarFila(Object... valores) {
        modelo.addRow(valores);
        actualizarContador();
    }

    public void setFilas(List<Object[]> filas) {
        modelo.setRowCount(0);
        if (filas != null) {
            for (Object[] fila : filas) {
                modelo.addRow(fila);
            }
        }
        actualizarContador();
    }

    public void limpiar() {
        modelo.setRowCount(0);
        actualizarContador();
    }

    public int getCantidadFilas() {
        return modelo.getRowCount();
    }

    public Object getValor(int fila, int columna) {
        return modelo.getValueAt(fila, columna);
    }

    public int getFilaSeleccionada() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? -1 : tabla.convertRowIndexToModel(fila);
    }

    public void setColumnasDerecha(int... columnas) {
        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : columnas) {
            tabla.getColumnModel().getColumn(c).setCellRenderer(derecha);
        }
    }

    public void setColumnasCentradas(int... columnas) {
        DefaultTableCellRenderer centro = new DefaultTableCellRenderer();
        centro.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c : columnas) {
            tabla.getColumnModel().getColumn(c).setCellRenderer(centro);
        }
    }

    public void setColumnaEstado(int columna) {
        tabla.getColumnModel().getColumn(columna).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor, boolean seleccionada,
                                                           boolean foco, int fila, int col) {
                Component c = super.getTableCellRendererComponent(t, valor, seleccionada, foco, fila, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!seleccionada) {
                    c.setForeground(Estilos.colorEstado(valor != null ? valor.toString() : null));
                }
                return c;
            }
        });
    }

    public void setAnchos(int... anchos) {
        for (int i = 0; i < anchos.length && i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    public void alHacerDobleClic(IntConsumer accion) {
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = getFilaSeleccionada();
                    if (fila >= 0) {
                        accion.accept(fila);
                    }
                }
            }
        });
    }

    public JTable getTabla() {
        return tabla;
    }

    private void actualizarContador() {
        lblRegistros.setText("Registros: " + modelo.getRowCount());
    }
}
