# Componentes de interfaz y reportes (Integrante 5)

Este documento explica cómo usar los componentes Swing comunes de SIGMA (tarjeta **UI-01**) y cómo funciona el módulo de reportes (tarjeta **UI-02**).

Regla del equipo: **no copiar ni rehacer estos componentes**. Si a un componente le falta algo, se pide al Integrante 5.

---

## UI-01 · Componentes reutilizables

Paquete: `com.sigma.presentacion.componentes`

| Clase | Para qué sirve |
|---|---|
| `Estilos` | Colores, tipografías, bordes, botones y formato de importes y fechas. |
| `TablaPanel` | Tabla de solo lectura con scroll, contador de registros y formatos de columna. |
| `BuscadorPanel` | Campo de búsqueda con botones "Buscar" y "Limpiar" (Enter también busca). |
| `DialogoConfirmacion` | Confirmaciones Sí/No y mensajes de aviso, error y éxito. |
| `FormularioBase` | Molde para formularios de alta y edición con "Guardar" y "Cancelar". |

### Estilos

```java
JButton btnGuardar = Estilos.botonPrimario("Guardar");     // azul
JButton btnCancelar = Estilos.botonSecundario("Cancelar");
panel.setBorder(Estilos.bordeSeccion("Datos del cliente"));
String importe = Estilos.moneda(servicio.getTotal());      // "$ 34.000,00"
String fecha = servicio.getFecha().format(Estilos.FORMATO_FECHA_HORA);
```

### TablaPanel

```java
TablaPanel tabla = new TablaPanel("Vehículos", "Patente", "Modelo", "Cliente", "Kilometraje");
tabla.setAnchos(90, 160, 180, 100);
tabla.setColumnasCentradas(0);
tabla.setColumnasDerecha(3);

for (Vehiculo v : vehiculos) {
    tabla.agregarFila(v.getPatente(), v.getModelo(), v.getNombreCliente(), v.getKilometraje());
}

tabla.alHacerDobleClic(fila -> abrirEdicion((String) tabla.getValor(fila, 0)));
int fila = tabla.getFilaSeleccionada();   // -1 si no hay selección
tabla.limpiar();
```

Para una columna de estado de orden con colores: `tabla.setColumnaEstado(indice)`.

### BuscadorPanel

```java
BuscadorPanel buscador = new BuscadorPanel("Patente:",
        texto -> cargarTabla(vehiculoService.buscarPorPatente(texto)),
        () -> cargarTabla(vehiculoService.listarTodos()));
panel.add(buscador, BorderLayout.NORTH);
```

El buscador no consulta la base de datos: entrega el texto a la acción del módulo. Si el campo está vacío, avisa al usuario y no ejecuta la búsqueda.

### DialogoConfirmacion

```java
if (DialogoConfirmacion.confirmarEliminacion(this, "el cliente " + cliente.getNombre())) {
    clienteService.eliminar(cliente.getIdCliente());
    DialogoConfirmacion.mostrarExito(this, "Cliente eliminado.");
}
DialogoConfirmacion.mostrarAviso(this, "La patente es obligatoria.");   // validaciones
DialogoConfirmacion.mostrarError(this, "No se pudo conectar con la base de datos.");
```

La opción preseleccionada en las confirmaciones es **"No"**, para evitar borrados accidentales.

### FormularioBase

```java
public class ClienteForm extends FormularioBase {
    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtContacto = new JTextField(25);

    public ClienteForm(Window owner) {
        super(owner, "Nuevo cliente");
        agregarCampo("Nombre", txtNombre, true);       // true = obligatorio (*)
        agregarCampo("Contacto", txtContacto, false);
    }

    @Override
    protected boolean guardar() {
        if (!validarObligatorio(txtNombre, "Nombre")) {
            return false;                              // el formulario queda abierto
        }
        // llamar al servicio del módulo
        return true;                                   // se cierra el formulario
    }
}

ClienteForm form = new ClienteForm(SwingUtilities.getWindowAncestor(this));
form.mostrar();
if (form.fueGuardado()) {
    recargarTabla();
}
```

---

## UI-02 · Consultas y reportes

| Clase | Capa | Responsabilidad |
|---|---|---|
| `presentacion.reportes.ReportePanel` | Presentación | Filtros, tarjetas de resumen y tabla de resultados. |
| `negocio.servicios.ReporteService` | Negocio | Filtra por fecha y estado, cuenta por estado y suma ingresos. |
| `negocio.modelo.ResultadoReporte` | Negocio | Resultado de solo lectura que devuelve el servicio. |

### Qué muestra

- **Filtros:** rango de fechas (por defecto, el mes actual) y estado (o todos).
- **Resumen:** cantidad de órdenes, finalizadas, mano de obra cobrada, repuestos cobrados, ingresos totales e importe en curso.
- **Órdenes por estado:** cantidad de órdenes de cada estado.
- **Tabla:** órdenes que cumplen el filtro, de la más reciente a la más antigua.

### Criterios de cálculo

- **Ingresos:** solo suman las órdenes **finalizadas**.
- **Importe en curso:** órdenes pendientes, confirmadas o en proceso (todavía no cobradas).
- **Canceladas:** se cuentan, pero no suman importes.
- La fecha "Hasta" incluye el día completo.
- No se recalculan importes: se usan los totales que ya guarda cada orden.

### Contrato con el Integrante 3 (Servicios del taller)

`ReporteService` obtiene las órdenes con `ServicioService.listarTodas()` y **no modifica** `ServicioDAO` ni `ServicioService`. Si en el futuro el volumen de datos lo justifica, se puede acordar con el Integrante 3 una consulta resumida en la base de datos sin cambiar la pantalla.

### Integración en la ventana principal (Integrante 1)

```java
pestanas.addTab("Reportes", new ReportePanel());
```

### Pruebas

`src/test/java/com/sigma/negocio/servicios/ReporteServiceTest.java` (7 pruebas). Usan datos en memoria, así que no necesitan MySQL.
