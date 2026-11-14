package com.sigma.negocio.modelo;

public final class Usuarios {
    private final long idUsuario;
    private final String nombre;
    private final String contacto;
    private final String username;
    private final Roles rol;
    private final boolean activo;

    public Usuarios(long idUsuario, String nombre, String contacto, String username, Roles rol, boolean activo) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.contacto = contacto;
        this.username = username;
        this.rol = rol;
        this.activo = activo;
    }

    public long getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContacto() {
        return contacto;
    }

    public String getUsername() {
        return username;
    }

    public Roles getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
