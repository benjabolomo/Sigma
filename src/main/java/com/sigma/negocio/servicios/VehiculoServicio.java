/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigma.negocio.servicios;

import com.sigma.datos.dao.VehiculoDAO;
import com.sigma.negocio.modelo.TipoVehiculo;
import com.sigma.negocio.modelo.Vehiculo;

import java.util.List;

public class VehiculoServicio {
    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();

    public boolean registrarVehiculo(Vehiculo vehiculo) throws Exception {
        // Regla: Patente única
        if (vehiculoDAO.buscarPorPatente(vehiculo.getPatente()) != null) {
            throw new Exception("Ya existe un vehículo registrado con la patente: " + vehiculo.getPatente());
        }
        // Regla: Cliente obligatorio
        if (vehiculo.getClienteId() <= 0) {
            throw new Exception("El vehículo debe estar asociado obligatoriamente a un cliente.");
        }
        return vehiculoDAO.insertar(vehiculo);
    }

    public Vehiculo buscarPorPatente(String patente) {
        return vehiculoDAO.buscarPorPatente(patente);
    }

    public List<Vehiculo> listarPorCliente(int clienteId) {
        return vehiculoDAO.listarPorCliente(clienteId);
    }

    public List<TipoVehiculo> listarTiposVehiculo() {
        return vehiculoDAO.listarTiposVehiculo();
    }
}
