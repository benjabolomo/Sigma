/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigma.negocio.servicios;

/*clase de ejemplo para poder manter estructura en el git.
Ejemplo: Aplicacion de validaciones de reglas de negocio antes de subir a db
*/

import com.sigma.datos.dao.ClienteDAO;
import com.sigma.negocio.modelo.Cliente;
import java.util.List;

public class ClienteServicio {
    private final ClienteDAO clienteDAO;

    public ClienteServicio() {
        this.clienteDAO = new ClienteDAO();
    }

    /**
     * Valida y registra un nuevo cliente.
     */
    public boolean registrarCliente(String nombre, String contacto) throws IllegalArgumentException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente no puede estar vacío.");
        }

        Cliente cliente = new Cliente(nombre.trim(), contacto != null ? contacto.trim() : null);
        return clienteDAO.insertar(cliente);
    }

    /**
     * Devuelve todos los clientes registrados.
     */
    public List<Cliente> obtenerClientes() {
        return clienteDAO.listarTodos();
    }
}
