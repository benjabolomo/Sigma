package com.sigma.negocio.modelo;

/**
 * Enumeración que representa los estados del ciclo de vida de una orden de servicio.
 * Permite validar las transiciones permitidas según las reglas de negocio del taller.
 */
public enum EstadoServicio {
    PENDIENTE("Pendiente") {
        @Override
        public boolean esTransicionValida(EstadoServicio nuevoEstado) {
            return nuevoEstado == EN_PROCESO || nuevoEstado == CONFIRMADO || nuevoEstado == CANCELADO;
        }
    },
    BORRADOR("Borrador") {
        @Override
        public boolean esTransicionValida(EstadoServicio nuevoEstado) {
            return nuevoEstado == CONFIRMADO || nuevoEstado == EN_PROCESO || nuevoEstado == CANCELADO;
        }
    },
    CONFIRMADO("Confirmado") {
        @Override
        public boolean esTransicionValida(EstadoServicio nuevoEstado) {
            return nuevoEstado == EN_PROCESO || nuevoEstado == CANCELADO;
        }
    },
    EN_PROCESO("En Proceso") {
        @Override
        public boolean esTransicionValida(EstadoServicio nuevoEstado) {
            return nuevoEstado == FINALIZADO || nuevoEstado == CANCELADO;
        }
    },
    FINALIZADO("Finalizado") {
        @Override
        public boolean esTransicionValida(EstadoServicio nuevoEstado) {
            return false; // Estado terminal
        }
    },
    CANCELADO("Cancelado") {
        @Override
        public boolean esTransicionValida(EstadoServicio nuevoEstado) {
            return false; // Estado terminal
        }
    };

    private final String nombreVisible;

    EstadoServicio(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    /**
     * Valida si la transición desde el estado actual hacia el nuevo estado es válida.
     */
    public abstract boolean esTransicionValida(EstadoServicio nuevoEstado);

    @Override
    public String toString() {
        return nombreVisible;
    }
}
