# Sigma - Sistema de Gestión de Taller Mecánico

Sistema de gestión integral para taller mecánico desarrollado en Java Swing con arquitectura de tres capas y persistencia en MySQL a través de JDBC.

## Tecnologías Utilizadas

* **Lenguaje:** Java 
* **Gestión de dependencias:** Apache Maven
* **Interfaz gráfica:** Java Swing
* **Base de datos:** MySQL
* **Conector JDBC:** `mysql-connector-j` 
* **Contenedores:** Docker / Docker Compose

---

## Arquitectura del Sistema

El proyecto está estructurado en tres capas independientes para separar responsabilidades y facilitar la mantenibilidad del código:

```text
com.sigma/
├── config/                  # Configuración global de la aplicación (AppConfig)
├── datos/
│   ├── conexion/            # Gestión del ciclo de vida de la conexión JDBC
│   └── dao/                 # Patrón DAO e implementación SQL (ClienteDAO, etc.)
├── negocio/
│   ├── modelo/              # Entidades del dominio (Cliente, Vehiculo, Servicio, Usuario)
│   └── servicios/           # Reglas de negocio y validaciones (ClienteServicio, etc.)
└── presentacion/
    ├── clientes/            # Formularios y paneles de clientes
    ├── vehiculos/           # Formularios y paneles de vehículos
    ├── servicios/           # Gestión de órdenes de trabajo
    └── componentes/         # Componentes UI reutilizables
```

### Principios de Diseño

* **Capa de Presentación (UI):** Renderizado de componentes Swing y captura de eventos de usuario. No ejecuta sentencias SQL ni reglas de negocio.
* **Capa de Negocio (Servicios y Modelo):** Validación de datos, aplicación de lógica de negocio y mediación entre la UI y la capa de acceso a datos.
* **Capa de Datos (DAO y Conexión):** Operaciones CRUD sobre la base de datos MySQL. Se utiliza `PreparedStatement` para parametrizar las consultas y prevenir inyección SQL.

---

## Base de Datos

El diseño de la base de datos `sigma_db` implementa tablas de catálogo para desacoplar el código de los valores estáticos.

### Tablas de Catálogo (`tipo_vehiculo` y `roles`)

Los tipos de vehículos y los roles de usuario se gestionan dinámicamente mediante tablas independientes:

* `tipo_vehiculo`: Define las categorías atendidas (ej. Auto, Camioneta, Utilitario, Moto).
* `roles`: Define los niveles de acceso del sistema (ej. ADMINISTRADOR, MECANICO, RECEPCIONISTA).

Este diseño permite agregar nuevos tipos o roles mediante inserciones en la base de datos sin modificar el código fuente de la aplicación.

---

## Configuración del Entorno de Desarrollo

### Opción 1: Despliegue con Docker (Recomendado)

El proyecto incluye una configuración de Docker Compose para desplegar el servidor MySQL con las tablas e inicialización previa.

1. Iniciar Docker Desktop.
2. Desde la raíz del proyecto, ejecutar:
   ```bash
   docker compose up -d
   ```
   El contenedor creará la base de datos `sigma_db` e importará automáticamente el esquema inicial desde `./db/init.sql`.

#### Datos de Conexión

* **Host:** `localhost`
* **Puerto:** `3307`
* **Base de Datos:** `sigma_db`
* **Usuario Administrador:** `root` / **Contraseña:** `root`
* **Usuario Aplicación:** `user` / **Contraseña:** `user`

---

### Opción 2: Instalación Manual (MySQL Local)

1. Crear la base de datos en su instancia local de MySQL:
   ```sql
   CREATE DATABASE sigma_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Ejecutar el script `./db/init.sql` sobre la base de datos creada.
3. Si la instancia utiliza un puerto distinto a `3307` o credenciales diferentes, ajustar la cadena de conexión en `com.sigma.datos.conexion.Conexion`:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/sigma_db?...";
   ```

---

## Compilación y Ejecución (NetBeans)

1. Seleccionar **File > Open Project...** y abrir la carpeta raíz del proyecto.
2. Hacer clic derecho sobre el proyecto y ejecutar **Clean and Build**.
3. Ejecutar la clase principal `com.sigma.Sigma` (o presionar `F6`).

---

## Flujo de Trabajo en Git

### Estrategia de Ramas

La rama `main` contiene únicamente código estable e integrado. Los desarrollos deben realizarse en ramas independientes según la funcionalidad asignada.

#### Proceso de Integración

1. **Sincronizar la rama principal:**
   ```bash
   git checkout main
   git pull origin main
   ```

2. **Crear una rama de trabajo:**
   ```bash
   git checkout -b feature/nombre-funcionalidad
   ```

3. **Registrar los cambios:**
   ```bash
   git add .
   git commit -m "feat: descripción concisa del cambio"
   ```

4. **Publicar la rama y solicitar revisión:**
   ```bash
   git push -u origin feature/nombre-funcionalidad
   ```
   Crear un Pull Request en GitHub hacia la rama `main` para su revisión e integración.

---

## Convenciones de Código

* **Gestión de Recursos:** Uso obligatorio de *try-with-resources* en las clases DAO para garantizar el cierre automático de `Connection`, `PreparedStatement` y `ResultSet`.
* **Nomenclatura en Base de Datos:** Nombres de tablas y columnas en minúsculas utilizando snake_case (`id_cliente`, `fecha_creacion`).
* **Manejo de Excepciones:** Registrar o propagar los errores de SQL hacia la capa de negocio evitando bloques `catch` vacíos.