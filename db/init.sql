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
    activo BOOLEAN NOT NULL DEFAULT TRUE,
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
    activo BOOLEAN NOT NULL DEFAULT TRUE,
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
    id_usuario BIGINT NULL,
    total_mano_obra DECIMAL(12,2) DEFAULT 0.00,
    total_repuestos DECIMAL(12,2) DEFAULT 0.00,
    total DECIMAL(12,2) DEFAULT 0.00,
    FOREIGN KEY (patente_vehiculo) REFERENCES vehiculo(patente) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Detalles del Servicio (Mano de Obra y Repuestos)
CREATE TABLE IF NOT EXISTS detalle_servicio (
    id_detalle BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_servicio BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (id_servicio) REFERENCES servicio(id_servicio) ON DELETE CASCADE
);

-- Datos iniciales obligatorios
INSERT IGNORE INTO tipo_vehiculo (id_tipo, descripcion) VALUES (1, 'Auto'), (2, 'Camioneta'), (3, 'Utilitario'), (4, 'Moto');
INSERT IGNORE INTO roles (id_rol, nombre_rol) VALUES (1, 'ADMINISTRADOR'), (2, 'MECANICO'), (3, 'RECEPCIONISTA');