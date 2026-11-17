package com.sigma.presentacion.componentes;

import com.sigma.negocio.modelo.EstadoServicio;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public final class Estilos {
    public static final Color PRIMARIO = new Color(0, 120, 215);
    public static final Color EXITO = new Color(0, 130, 60);
    public static final Color ADVERTENCIA = new Color(204, 102, 0);
    public static final Color PELIGRO = new Color(200, 30, 30);
    public static final Color NEUTRO = Color.DARK_GRAY;
    public static final Color FONDO_SUAVE = new Color(245, 245, 245);
    public static final Color BORDE = new Color(210, 210, 210);

    public static final Font TITULO = new Font("SansSerif", Font.BOLD, 16);
    public static final Font SECCION = new Font("SansSerif", Font.BOLD, 12);
    public static final Font NORMAL = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font PEQUENA = new Font("SansSerif", Font.ITALIC, 11);
    public static final Font DESTACADA = new Font("SansSerif", Font.BOLD, 15);

    public static final int ALTO_FILA = 24;
    public static final int MARGEN = 12;

    private static final DecimalFormat FORMATO_MONEDA = new DecimalFormat("$ #,##0.00");
    public static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Estilos() {
    }

    public static String moneda(BigDecimal importe) {
        return FORMATO_MONEDA.format(importe != null ? importe : BigDecimal.ZERO);
    }

    public static Border bordeSeccion(String titulo) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), titulo,
                TitledBorder.LEFT, TitledBorder.TOP, SECCION);
    }

    public static JButton botonPrimario(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setBackground(PRIMARIO);
        boton.setForeground(Color.WHITE);
        boton.setOpaque(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        return boton;
    }

    public static JButton botonSecundario(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        boton.setFocusPainted(false);
        return boton;
    }

    public static Color colorEstado(EstadoServicio estado) {
        if (estado == null) {
            return NEUTRO;
        }
        switch (estado) {
            case EN_PROCESO:
                return ADVERTENCIA;
            case FINALIZADO:
                return EXITO;
            case CANCELADO:
                return PELIGRO;
            default:
                return NEUTRO;
        }
    }

    public static Color colorEstado(String textoEstado) {
        if (textoEstado != null) {
            for (EstadoServicio e : EstadoServicio.values()) {
                if (e.getNombreVisible().equalsIgnoreCase(textoEstado) || e.name().equalsIgnoreCase(textoEstado)) {
                    return colorEstado(e);
                }
            }
        }
        return NEUTRO;
    }
}
