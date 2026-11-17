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

Los tipos de vehículos pueden ampliarse en la base de datos. Los roles son un catálogo fijo que debe coincidir con el enum `Roles`; sus permisos se definen en código.

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

#### Reiniciar y reconstruir la base de datos desde cero

Si ya tenías un contenedor levantado con una versión anterior de la base de datos y necesitás aplicar el nuevo `./db/init.sql` limpio:

1. Detener el contenedor y **eliminar el volumen de datos persistentes** (`-v`):
   ```bash
   docker compose down -v
   ```
2. Volver a levantar el contenedor para que Docker ejecute nuevamente `./db/init.sql`:
   ```bash
   docker compose up -d
   ```

*(Nota: Si solo querés detener o reiniciar el contenedor conservando los datos cargados, usá `docker compose stop` / `docker compose start` o `docker compose restart`, sin la bandera `-v`).*

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

## Administración local de usuarios

Si ya existe una base de Sigma, aplicar una sola vez `db/migrations/001_usuario_activo.sql` sobre `sigma_db`. Las instalaciones nuevas ya incluyen la columna `activo` en el esquema inicial.

Desde una terminal de Windows, compilar y copiar las dependencias de ejecución con Maven.

```powershell
mvn compile dependency:copy-dependencies
java -cp "target/classes;target/dependency/*" com.sigma.negocio.servicios.AdminConsola crear-admin
java -cp "target/classes;target/dependency/*" com.sigma.negocio.servicios.AdminConsola restablecer-admin ana
```

En el último comando, reemplazar `ana` por el nombre de usuario del administrador activo. La terminal pide la contraseña dos veces con entrada oculta. Los comandos usan la conexión MySQL de `Conexion` y requieren acceso a esa instalación.

## Roles y permisos: contrato de integración

La autorización está disponible en negocio; todavía no está conectada a las pantallas ni a los CRUD de clientes, vehículos y servicios. No se agregan tablas de permisos ni migraciones nuevas para esta funcionalidad.

| Permiso | Administrador | Recepcionista | Mecánico |
|---|---|---|---|
| `CONSULTAR_CLIENTES` | Sí | Sí | Sí |
| `GESTIONAR_CLIENTES` | Sí | Sí | No |
| `CONSULTAR_VEHICULOS` | Sí | Sí | Sí |
| `GESTIONAR_VEHICULOS` | Sí | Sí | No |
| `GESTIONAR_SERVICIOS` | Sí | Sí | Sí |
| `GESTIONAR_USUARIOS` | Sí | No | No |

Gestionar clientes o vehículos autoriza alta, edición y borrado; consultar autoriza lectura. Gestionar servicios incluye consulta, alta, edición y borrado. Gestionar usuarios permite listar, crear y desactivar mediante `UsuarioServicio`; no incluye edición de roles. La matriz se centraliza en `Roles.tienePermiso`, pero para autorizar una sesión se debe usar `UsuarioServicio`, que consulta el rol y estado activo actuales en la base.

### Presentación y negocio

La sesión se obtiene exclusivamente de `iniciarSesion`. Para habilitar acciones en Swing (hacer las consultas JDBC fuera del hilo de eventos):

```java
boolean puedeEditar = usuarios.tienePermiso(sesion, Permiso.GESTIONAR_VEHICULOS);
// Aplicar el resultado a los controles en el hilo de eventos de Swing.
```

Cada operación de negocio debe verificar nuevamente el permiso antes de acceder a su DAO, aunque el botón esté oculto o deshabilitado:

```java
usuarios.verificarPermiso(sesion, Permiso.CONSULTAR_VEHICULOS);
// Consultar los vehículos existentes desde el DAO.

usuarios.verificarPermiso(sesion, Permiso.GESTIONAR_VEHICULOS);
// Validar y guardar una modificación del vehículo o de su propietario.
```

Ambos métodos declaran `throws UsuarioException`. `tienePermiso` devuelve `false` si faltan sesión o permiso, o si el usuario no existe, está inactivo o no tiene autorización. `verificarPermiso` rechaza esos casos con `SIN_PERMISO`. Un fallo de base produce `ERROR_PERSISTENCIA`; no debe interpretarse como autorización. El perfil de `sesion.getUsuario()` corresponde al inicio de sesión y no sirve como comprobación vigente de permisos.

### Carga de servicios por el mecánico

El selector de vehículos requiere `CONSULTAR_VEHICULOS` y la consulta de su cliente requiere `CONSULTAR_CLIENTES`. Estos permisos no habilitan edición ni pantallas de mantenimiento para el mecánico. Al guardar se exige `GESTIONAR_SERVICIOS`, se valida que el vehículo exista y se persiste su patente en `servicio.patente_vehiculo`. El cliente se obtiene de `vehiculo.id_cliente`; no se acepta un propietario alternativo ni se actualizan cliente o vehículo como efecto de guardar el servicio.

Cambiar el propietario es una operación independiente de gestión de vehículos y requiere `GESTIONAR_VEHICULOS`. Los servicios de negocio deben llamar a la autorización; presentación no debe acceder directamente a los DAO. La consulta de permisos no mantiene un bloqueo hasta la futura escritura de otro servicio: una operación ya autorizada puede terminar si la cuenta se desactiva simultáneamente; las comprobaciones posteriores se deniegan.

Aceptación pendiente de la integración: un mecánico selecciona un vehículo ya cargado, consulta su cliente y registra un servicio. Un intento de reasignar el propietario se rechaza y `vehiculo.id_cliente` permanece intacto.

### Administrador único y baja lógica

`crear-admin` crea el primer administrador. Tanto esa ruta como `crearUsuario` rechazan un segundo administrador con `ADMIN_YA_EXISTE`, incluso si el existente está inactivo. Un bloqueo transaccional del rol serializa las altas. `desactivarUsuario` nunca permite desactivar una cuenta administradora; para los demás usuarios mantiene `activo = FALSE`, preserva los servicios asociados y es idempotente. Se conserva la recuperación de contraseña mediante la consola.

La protección cubre las operaciones de negocio, no escrituras directas en MySQL ni llamadas directas al DAO. Antes de integrar sobre una base existente, ejecutar:

```sql
SELECT COUNT(*) AS administradores
FROM usuario u JOIN roles r ON r.id_rol = u.id_rol
WHERE r.nombre_rol = 'ADMINISTRADOR';

SELECT u.id_usuario, u.username, u.activo
FROM usuario u JOIN roles r ON r.id_rol = u.id_rol
WHERE r.nombre_rol = 'ADMINISTRADOR';
```

Si hay más de uno, resolver las cuentas heredadas manualmente antes de habilitar la funcionalidad. No se elimina, desactiva ni elige automáticamente ninguna cuenta. Si el único administrador está inactivo por una modificación externa, requiere reparación manual; no se permite crear otro.

### Autopruebas

Las pruebas son programas con `main`, sin dependencias adicionales; `mvn test` no las ejecuta automáticamente. Con JDK 25 y Maven:

```powershell
mvn test-compile dependency:copy-dependencies
java -cp "target/test-classes;target/classes;target/dependency/*" com.sigma.negocio.servicios.PermisosPrueba
```

Para integración, crear una base MySQL **exclusiva de pruebas y vacía**, cargar `db/init.sql` y proporcionar su contraseña mediante `SIGMA_TEST_DB_PASSWORD`. No usar `sigma_db`: la prueba crea y modifica registros, incluyendo bajas y un borrado de prueba; deja datos al finalizar. Usar una base nueva para cada ejecución.

```powershell
java -cp "target/test-classes;target/classes;target/dependency/*" com.sigma.negocio.servicios.UsuarioServicioIntegracion "jdbc:mysql://localhost:3307/sigma_roles_test" user
```

La integración verifica altas iniciales concurrentes, administrador único e imborrable desde negocio, credenciales, matriz por sesión, cambios externos de rol, sesiones inexistentes o inactivas, errores de conexión y conservación del historial tras la baja lógica.

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
