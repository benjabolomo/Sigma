package com.sigma.negocio.modelo;

public class Vehiculo {
    private int id;
    private String patente;
    private String marca;
    private String modelo;
    private int anio;
    private int clienteId;
    private int tipoVehiculoId;
    private String nombreCliente;
    
    // Campos requeridos por ServicioDAO
    private int kilometraje;
    private long idCliente;
    private int idTipo;
    private String tipoDescripcion;

    public Vehiculo() {}

    public Vehiculo(int id, String patente, String marca, String modelo, int anio, int clienteId, int tipoVehiculoId) {
        this.id = id;
        this.patente = patente;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.clienteId = clienteId;
        this.idCliente = clienteId; // Sincronizado
        this.tipoVehiculoId = tipoVehiculoId;
        this.idTipo = tipoVehiculoId; // Sincronizado
    }

    // Getters y Setters estándar
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPatente() { return patente; }
    public void setPatente(String patente) { this.patente = patente; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { 
        this.clienteId = clienteId; 
        this.idCliente = clienteId;
    }

    public int getTipoVehiculoId() { return tipoVehiculoId; }
    public void setTipoVehiculoId(int tipoVehiculoId) { 
        this.tipoVehiculoId = tipoVehiculoId; 
        this.idTipo = tipoVehiculoId;
    }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    // --- Métodos específicos que reclamaba ServicioDAO ---
    public int getKilometraje() { return kilometraje; }
    public void setKilometraje(int kilometraje) { this.kilometraje = kilometraje; }

    public long getIdCliente() { return idCliente; }
    public void setIdCliente(long idCliente) { 
        this.idCliente = idCliente; 
        this.clienteId = (int) idCliente;
    }

    public int getIdTipo() { return idTipo; }
    public void setIdTipo(int idTipo) { 
        this.idTipo = idTipo; 
        this.tipoVehiculoId = idTipo;
    }

    public String getTipoDescripcion() { return tipoDescripcion; }
    public void setTipoDescripcion(String tipoDescripcion) { this.tipoDescripcion = tipoDescripcion; }

    @Override
    public String toString() {
        return patente + " - " + marca + " " + modelo;
    }
}
