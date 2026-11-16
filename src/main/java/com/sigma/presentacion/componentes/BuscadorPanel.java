package com.sigma.presentacion.componentes;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.util.function.Consumer;

public class BuscadorPanel extends JPanel {
    private final JTextField txtBusqueda;

    public BuscadorPanel(String etiqueta, Consumer<String> alBuscar, Runnable alLimpiar) {
        super(new FlowLayout(FlowLayout.LEFT, 8, 6));

        JLabel lbl = new JLabel(etiqueta);
        lbl.setFont(Estilos.NORMAL);
        txtBusqueda = new JTextField(14);
        txtBusqueda.setFont(Estilos.NORMAL);

        JButton btnBuscar = Estilos.botonPrimario("Buscar");
        JButton btnLimpiar = Estilos.botonSecundario("Limpiar");

        Runnable buscar = () -> {
            String texto = getTexto();
            if (texto.isEmpty()) {
                DialogoConfirmacion.mostrarAviso(this, "Ingrese un valor para buscar.");
                txtBusqueda.requestFocusInWindow();
                return;
            }
            alBuscar.accept(texto);
        };

        btnBuscar.addActionListener(e -> buscar.run());
        txtBusqueda.addActionListener(e -> buscar.run());
        btnLimpiar.addActionListener(e -> {
            limpiar();
            if (alLimpiar != null) {
                alLimpiar.run();
            }
        });

        add(lbl);
        add(txtBusqueda);
        add(btnBuscar);
        add(btnLimpiar);
    }

    public String getTexto() {
        return txtBusqueda.getText().trim();
    }

    public void limpiar() {
        txtBusqueda.setText("");
    }

    public JTextField getCampo() {
        return txtBusqueda;
    }
}
