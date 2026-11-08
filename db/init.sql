-- Catalogo de tipos de vehiculos 
CREATE TABLE IF NOT EXISTS tipo_vehiculo (
    id_tipo INT AUTO_INCREMENT PRIMARY KEY,
    descripcion VARCHAR(50) NOT NULL UNIQUE
);

-- Clientes
CREATE TABLE IF NOT EXISTS cliente (
    id_cliente BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    contacto VARCHAR(100),
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Roles de usuario
CREATE TABLE IF NOT EXISTS roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE
);

-- Usuarios del sistema
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    contacto VARCHAR(100),
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);

-- Vehiculos
CREATE TABLE IF NOT EXISTS vehiculo (
    patente VARCHAR(15) PRIMARY KEY,
    modelo VARCHAR(100) NOT NULL,
    anio INT,
    kilometraje INT DEFAULT 0,
    id_cliente BIGINT NOT NULL,
    id_tipo INT NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (id_tipo) REFERENCES tipo_vehiculo(id_tipo) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Encabezado del Servicio
CREATE TABLE IF NOT EXISTS servicio (
    id_servicio BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    observacion TEXT,
    estado ENUM('PENDIENTE', 'EN_PROCESO', 'FINALIZADO', 'CANCELADO') DEFAULT 'PENDIENTE',
    patente_vehiculo VARCHAR(15) NOT NULL,
    id_usuario BIGINT NOT NULL,
    FOREIGN KEY (patente_vehiculo) REFERENCES vehiculo(patente) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Datos iniciales obligatorios
INSERT IGNORE INTO tipo_vehiculo (id_tipo, descripcion) VALUES (1, 'Auto'), (2, 'Camioneta'), (3, 'Utilitario'), (4, 'Moto');
INSERT IGNORE INTO roles (id_rol, nombre_rol) VALUES (1, 'ADMINISTRADOR'), (2, 'MECANICO'), (3, 'RECEPCIONISTA');