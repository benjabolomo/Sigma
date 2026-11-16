/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigma.negocio.servicios;

import com.sigma.negocio.modelo.Cliente;
import java.util.List;

public interface ClienteRepository {
    boolean insertar(Cliente cliente);
    boolean actualizar(Cliente cliente);
    boolean bajaLogica(int id);
    List<Cliente> listar();
    Cliente buscarPorDni(String dni);
}
