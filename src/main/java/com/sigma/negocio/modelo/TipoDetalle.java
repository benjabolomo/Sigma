package com.sigma.negocio.modelo;

/**
 * Representa la tipología de un ítem dentro de una orden de servicio.
 * Discrimina entre costos de mano de obra y repuestos utilizados.
 */
public enum TipoDetalle {
    MANO_DE_OBRA("Mano de Obra"),
    REPUESTO("Repuesto");

    private final String descripcion;

    TipoDetalle(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
