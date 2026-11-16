package com.sigma.presentacion.componentes;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

public abstract class FormularioBase extends JDialog {
    private final JPanel pnlCampos;
    private final GridBagConstraints gbc;
    private int filaActual = 0;
    private boolean guardado = false;

    protected FormularioBase(Window owner, String titulo) {
        super(owner, titulo, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBorder(new EmptyBorder(Estilos.MARGEN, Estilos.MARGEN, Estilos.MARGEN, Estilos.MARGEN));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Estilos.TITULO);
        principal.add(lblTitulo, BorderLayout.NORTH);

        pnlCampos = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        principal.add(pnlCampos, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        JLabel lblObligatorios = new JLabel("(*) Campos obligatorios");
        lblObligatorios.setFont(Estilos.PEQUENA);
        JButton btnGuardar = Estilos.botonPrimario("Guardar");
        JButton btnCancelar = Estilos.botonSecundario("Cancelar");
        btnGuardar.addActionListener(e -> {
            if (guardar()) {
                guardado = true;
                dispose();
            }
        });
        btnCancelar.addActionListener(e -> dispose());
        pnlBotones.add(lblObligatorios);
        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnCancelar);
        principal.add(pnlBotones, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnGuardar);
        setContentPane(principal);
    }

    protected void agregarCampo(String etiqueta, JComponent campo, boolean obligatorio) {
        JLabel lbl = new JLabel(etiqueta + (obligatorio ? " (*)" : "") + ":");
        lbl.setFont(Estilos.NORMAL);

        gbc.gridx = 0;
        gbc.gridy = filaActual;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        pnlCampos.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        pnlCampos.add(campo instanceof JTextArea ? new JScrollPane(campo) : campo, gbc);

        filaActual++;
    }

    // Devuelve true si se guardó correctamente y el formulario debe cerrarse.
    protected abstract boolean guardar();

    public void mostrar() {
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    public boolean fueGuardado() {
        return guardado;
    }

    protected boolean validarObligatorio(JTextField campo, String nombreCampo) {
        if (campo.getText().trim().isEmpty()) {
            DialogoConfirmacion.mostrarAviso(this, "El campo \"" + nombreCampo + "\" es obligatorio.");
            campo.requestFocusInWindow();
            return false;
        }
        return true;
    }

    protected boolean validarEnteroNoNegativo(JTextField campo, String nombreCampo) {
        try {
            if (Integer.parseInt(campo.getText().trim()) >= 0) {
                return true;
            }
        } catch (NumberFormatException ignored) {
        }
        DialogoConfirmacion.mostrarAviso(this, "El campo \"" + nombreCampo + "\" debe ser un número entero mayor o igual a 0.");
        campo.requestFocusInWindow();
        return false;
    }
}
