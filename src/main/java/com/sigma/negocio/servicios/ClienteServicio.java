/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigma.negocio.servicios;

import com.sigma.datos.dao.ClienteDAO;
import com.sigma.negocio.modelo.Cliente;
import java.util.List;

public class ClienteServicio {
    private ClienteDAO clienteDAO = new ClienteDAO();

    public boolean registrarCliente(Cliente cliente) throws Exception {
        // Validar que no exista un cliente activo con el mismo DNI
        Cliente existente = clienteDAO.buscarPorDni(cliente.getDni());
        if (existente != null) {
            throw new Exception("Ya existe un cliente registrado con el DNI: " + cliente.getDni());
        }
        return clienteDAO.insertar(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteDAO.listar();
    }

    public boolean actualizarCliente(Cliente cliente) {
        return clienteDAO.actualizar(cliente);
    }

    public boolean bajaLogicaCliente(int id) {
        return clienteDAO.bajaLogica(id);
    }

    public List<Cliente> obtenerClientes() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
