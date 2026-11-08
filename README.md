# 🛠️ Sigma - Sistema de Gestión de Taller Mecánico

Proyecto universitario de gestión integral para taller mecánico desarrollado en **Java Swing**, utilizando una **arquitectura de 3 capas** (Presentación, Negocio y Datos) y persisitiendo la información en **MySQL 8.0** mediante JDBC puro.

---

## 📐 Stack Tecnológico

* **Lenguaje:** Java (JDK 17+)
* **Gestor de Construcción:** Maven
* **Interfaz Gráfica:** Java Swing
* **Base de Datos:** MySQL 8.0
* **Conector JDBC:** `mysql-connector-j` (v9.1.0)
* **Contenerización:** Docker / Docker Compose

---

## 📂 Arquitectura del Proyecto (3 Capas)

El sistema implementa una separación estricta de responsabilidades en tres capas para garantizar mantenibilidad, escalabilidad y legibilidad de código:

```text
com.sigma/
├── config/                  # Configuración global de la aplicación (AppConfig)
├── datos/
│   ├── conexion/            # Gestión del ciclo de vida de la conexión JDBC (Conexion)
│   └── dao/                 # Patrón DAO: Implementación de la persistencia SQL (ClienteDAO, etc.)
├── negocio/
│   ├── modelo/              # Entidades del dominio (Cliente, Vehiculo, Servicio, Usuario, etc.)
│   └── servicios/           # Capa de Servicio: Reglas de negocio y validaciones (ClienteServicio, etc.)
└── presentacion/
    ├── clientes/            # Vistas y paneles Swing para clientes (ClientePanel)
    ├── vehiculos/           # Vistas y paneles Swing para vehículos (VehiculoPanel)
    ├── servicios/           # Vistas y paneles Swing para órdenes de trabajo (ServicioPanel)
    └── componentes/         # Componentes visuales reutilizables (TablaPanel, etc.)
```

### Reglas de la Arquitectura:
1. **Presentación (UI):** Solamente responde a eventos de usuario y renderiza componentes Swing. **Prohibido** ejecutar sentencias SQL o validaciones de negocio complejas aquí.
2. **Negocio (Services & Model):** Modela la información del sistema y aplica reglas de negocio (validar patentes, comprobar estados, verificar campos obligatorios). Comunica la UI con la capa de datos.
3. **Datos (DAO & Conexión):** Ejecuta las operaciones CRUD directamente sobre MySQL. Utiliza obligatoriamente `PreparedStatement` con comodines `?` para prevenir vulnerabilidades de **Inyección SQL**.

---

## 🗄️ Base de Datos y Tablas Catálogo (Maestros)

La base de datos `sigma_db` está diseñada bajo principios de normalización y alta escalabilidad.

### Tablas Maestras / Catálogo (`tipo_vehiculo` y `roles`)
En lugar de hardcodear tipos de vehículos o roles en código Java o en columnas SQL estáticas, utilizamos tablas maestro de catálogo:

* **`tipo_vehiculo`**: Registra los tipos de vehículos (`Auto`, `Camioneta`, `Utilitario`, `Moto`).
* **`roles`**: Registra los roles de acceso del sistema (`ADMINISTRADOR`, `MECANICO`, `RECEPCIONISTA`).

> 💡 **Nota sobre Escalabilidad:** Si el día de mañana el taller empieza a atender *Cuatriciclos* o *Tractores*, **no se modifica la estructura de la base de datos ni el código Java**. Simplemente se agrega un nuevo registro a la tabla `tipo_vehiculo` mediante un `INSERT`. La tabla crece en filas, manteniendo el sistema 100% extensible.

---

## 🚀 Guía de Instalación y Ejecución Local

### Método 1: Con Docker Desktop (OPCIÓN RECOMENDADA)

Es el método estándar del proyecto. Permite tener exactamente la misma base de datos sin configurar nada manualmente.

1. **Asegurarse de tener Docker Desktop iniciado.**
2. Abre la terminal o consola PowerShell en la raíz del proyecto (`C:\...\sigma`).
3. Ejecuta el comando:
   ```bash
   docker compose up -d
   ```
4. Docker descargará la imagen de MySQL, creará el contenedor `mysql-sigma`, la base de datos `sigma_db` e importará automáticamente todas las tablas del archivo `./db/init.sql`.

#### Credenciales de Conexión (Docker):
* **Host:** `localhost`
* **Puerto:** `3307` *(Mapeado para evitar conflictos si tienes otro MySQL nativo)*
* **Database:** `sigma_db`
* **Superusuario (Root):** `root` | **Password:** `root`
* **Usuario App:** `user` | **Password:** `user`

---

### Método 2: Instalación Manual sin Docker (MySQL Local / XAMPP)

Si no dispones de Docker en tu máquina secundaria:

1. Inicia tu servidor local de MySQL (Workbench, phpMyAdmin o XAMPP).
2. Crea una nueva base de datos llamada `sigma_db`:
   ```sql
   CREATE DATABASE sigma_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Abre el archivo `./db/init.sql` ubicado en el proyecto e impórtalo/ejecútalo en tu cliente MySQL sobre `sigma_db`.
4. **Importante:** Si tu MySQL corre en el puerto por defecto `3306` o usa otra contraseña, debes actualizar la cadena de conexión en la clase `com.sigma.datos.conexion.Conexion.java`:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/sigma_db?...";
   ```

---

### Ejecución del Proyecto desde NetBeans

1. Abre NetBeans y ve a **File -> Open Project...**
2. Selecciona la carpeta raíz `sigma` (NetBeans la reconocerá como un proyecto Maven).
3. Haz clic derecho sobre el proyecto `sigma` -> **Clean and Build**.
4. Haz clic derecho sobre la clase `Sigma.java` (`com.sigma`) y selecciona **Run File** (o presiona `F6`).
5. En la consola de NetBeans debe aparecer: `¡Conexión exitosa a MySQL en Docker!`.

---

## 🌿 Flujo de Trabajo con Git y GitHub (REGLAS OBLIGATORIAS)

Para trabajar organizados sin pisar el código de los demás integrantes, nos manejamos mediante **Ramas (Branches)** y **Pull Requests (PR)**.

### ⚠️ REGLA DE ORO: Prohibido hacer `git push` directo sobre `main`

La rama `main` es el código de producción del grupo. Nadie hace cambios directamente sobre ella.

---

### Paso a Paso para Desarrollar un Módulo:

#### 1. Actualizar tu entorno local
Antes de empezar a trabajar en un nuevo módulo, colócate en `main` y descarga la última versión aprobada:
```bash
git checkout main
git pull origin main
```

#### 2. Crear una nueva rama según tu módulo asignado
Crea y cámbiate a una rama descriptiva de la tarea:
* **Integrante 2 (Clientes y Vehículos):** `git checkout -b feature/clientes-vehiculos`
* **Integrante 3 (Servicios del Taller):** `git checkout -b feature/servicios`
* **Integrante 4 (Usuarios y Roles):** `git checkout -b feature/usuarios-roles`
* **Integrante 5 (Componentes Swing y Reportes):** `git checkout -b feature/interfaz-reportes`

#### 3. Confirmar cambios con Commits descriptivos
Haz commits frecuentes y con mensajes claros:
```bash
git add .
git commit -m "Feat: Implementado alta de clientes en ClienteDAO y validación en ClienteServicio"
```

#### 4. Subir tu rama a GitHub
Sube únicamente tu rama de trabajo:
```bash
git push -u origin feature/clientes-vehiculos
```

#### 5. Crear un Pull Request (PR)
1. Ve a la página del repositorio en GitHub.
2. Haz clic en la opción **Compare & pull request**.
3. Escribe un título descriptivo y explica brevemente los cambios que agregaste.
4. El coordinador de arquitectura (Integrante 1) o un compañero revisará el código y realizará el **Merge** a `main`.

---

## 🧼 Buenas Prácticas de Código

1. **Gestión de Conexiones:** Siempre utiliza bloques *try-with-resources* en las clases DAO para garantizar que las conexiones, `PreparedStatement` y `ResultSet` se cierren de forma automática sin saturar MySQL:
   ```java
   try (Connection conn = Conexion.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
       // Operaciones SQL...
   } catch (SQLException e) {
       System.err.println("Error: " + e.getMessage());
   }
   ```
2. **Nomenclatura de BD:** Los nombres de tablas y columnas utilizan siempre minúsculas con guiones bajos (`id_cliente`, `fecha_creacion`), evitando tildes o caracteres especiales (`ñ`).
3. **Manejo de Excepciones:** No dejes bloques `catch` vacíos. Muestra un mensaje comprensible con `System.err.println()` o lánzalo a la capa de negocio para notificar al usuario.