/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigma.negocio.modelo;

//clase de ejemplo para poder manter estructura en el git.

import java.sql.Timestamp;

public class Cliente {
    private Long idCliente;
    private String nombre;
    private String contacto;
    private Timestamp fechaCreacion;

    public Cliente() {
    }

    public Cliente(String nombre, String contacto) {
        this.nombre = nombre;
        this.contacto = contacto;
    }

    public Cliente(Long idCliente, String nombre, String contacto, Timestamp fechaCreacion) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.contacto = contacto;
        this.fechaCreacion = fechaCreacion;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        return nombre + " (" + (contacto != null ? contacto : "Sin contacto") + ")";
    }
}

