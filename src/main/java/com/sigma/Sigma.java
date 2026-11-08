/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.sigma;

import com.sigma.datos.conexion.Conexion;
import java.sql.Connection;
import java.sql.SQLException;

public class Sigma {

   public static void main(String[] args) {
        try (Connection conn = Conexion.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Conexion exitosa a MySQL en Docker!");
            }
        } catch (SQLException e) {
            System.err.println("Error de conexion: " + e.getMessage());
        }
    }
}
