package com.sigma;

import com.sigma.presentacion.componentes.VentanaPrincipal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Sigma {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(VentanaPrincipal::iniciar);
    }
}