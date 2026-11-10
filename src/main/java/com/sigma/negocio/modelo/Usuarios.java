/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigma.negocio.modelo;

/**
 *
 * @author megaj
 */
public class Usuarios {
    private final long idu;
    private final string nombre;
    private final string contacto;
    private final string username;
    private final string email;
    private final string pass_hash;
    private final rol rol;
   
    
    public usuario (long id, string nombre, string contacto,  string username, string email, string pass_hash, rol rol) {
        this.id = id;
        this.nombre = nombre;
        this.contacto = contacto;
        this.username = username;
        this.email = email;
        this.pass_hash = pass_hash;
    }
    
   public string getUN(){
     return username;   
    }
}
