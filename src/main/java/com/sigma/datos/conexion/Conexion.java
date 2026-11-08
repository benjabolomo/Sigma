/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigma.datos.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author benja
 */
public class Conexion {
    // Configuración de conexión según tu docker-compose.yml
  private static final String URL = "jdbc:mysql://localhost:3307/sigma_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    /**
     * Obtiene una nueva conexión a la base de datos.
     * @return Connection objeto de conexión activo.
     * @throws SQLException si ocurre un error al conectar.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Cierra de forma segura una conexión abierta.
     * @param conn Objeto Connection a cerrar.
     */
    public static void cerrarConexion(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}
