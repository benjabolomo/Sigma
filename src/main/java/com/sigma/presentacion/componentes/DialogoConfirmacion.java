package com.sigma.presentacion.componentes;

import javax.swing.JOptionPane;
import java.awt.Component;

public final class DialogoConfirmacion {
    private DialogoConfirmacion() {
    }

    public static boolean confirmar(Component padre, String mensaje) {
        Object[] opciones = {"Sí", "No"};
        int respuesta = JOptionPane.showOptionDialog(padre, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opciones, opciones[1]); // "No" queda seleccionado por defecto
        return respuesta == 0;
    }

    public static boolean confirmarEliminacion(Component padre, String queSeElimina) {
        return confirmar(padre, "¿Seguro que desea eliminar " + queSeElimina + "?\nEsta acción no se puede deshacer.");
    }

    public static void mostrarAviso(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    public static void mostrarError(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void mostrarExito(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}
