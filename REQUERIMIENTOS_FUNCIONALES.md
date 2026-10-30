# 📋 Especificación de Requerimientos Funcionales — Sirbull Pet Clinic

Este documento detalla los **Requerimientos Funcionales (RF)** del **Sistema Integral de Gestión Veterinaria Sirbull Pet Clinic**, abarcando los módulos clínicos, administrativos, logísticos, comerciales (POS / Tienda), analíticos, de seguridad y el Portal del Cliente.

---

## 1. Actores y Roles del Sistema

El sistema implementa Control de Acceso Basado en Roles (**RBAC**) con los siguientes actores:

| Actor / Rol | Descripción General |
| :--- | :--- |
| **Administrador** | Control total del sistema: gestión de usuarios, auditoría, reportes analíticos, configuración de horarios y pagos, inventario, proveedores, clientes, citas, atención clínica y ventas. |
| **Veterinario** | Enfoque clínico: atención de citas, registro de consultas, diagnósticos, recetas, órdenes de exámenes auxiliares, referencias médicas, aplicación de vacunas y alertas sanitarias. |
| **Recepcionista** | Enfoque operativo y comercial: recepción y registro de clientes/mascotas, gestión de agenda de citas, punto de venta (POS), ingresos de mercadería, almacenes, devoluciones y reportes. |
| **Cliente** | Propietario de mascotas con acceso al Portal del Cliente y Tienda: consulta de historial clínico, recetas y exámenes de sus mascotas, reserva/reprogramación/cancelación y pago de citas, y compras en línea. |
| **Visitante (Público)** | Usuario no autenticado que navega por la página principal (Landing), artículos del Blog veterinario, catálogo de la Tienda y formularios de registro o recuperación de cuenta. |

---

## 2. Catálogo de Requerimientos Funcionales

### 🔐 Módulo 1: Autenticación, Seguridad y Control de Acceso

* **RF-01 — Inicio de Sesión y Emisión de Token JWT:**
  El sistema debe permitir a los usuarios iniciar sesión (`POST /token`) ingresando su nombre de usuario (`username`) o correo electrónico (`email`) junto con su contraseña. El sistema validará que la cuenta esté activa (`is_active = True`), generará un token JWT con el rol del usuario, aplicará un límite de velocidad (*Rate Limiting* de 5 intentos por minuto por IP) y registrará el evento `LOGIN` en el log de auditoría.
* **RF-02 — Autoregistro de Clientes:**
  El sistema debe permitir a un visitante registrarse como cliente (`POST /api/registro-cliente`) proporcionando usuario, contraseña, DNI, nombre completo, teléfono y correo electrónico, validando que el nombre de usuario, DNI y teléfono no se encuentren duplicados y creando simultáneamente la cuenta en `usuarios` (con rol `Cliente`) y el perfil en `clientes`.
* **RF-03 — Recuperación y Restablecimiento de Contraseña:**
  El sistema debe permitir solicitar la recuperación de contraseña por correo electrónico (`POST /api/recuperar-password`) generando un token criptográfico aleatorio almacenado mediante hash SHA-256 con vigencia de 30 minutos e invalidando tokens previos sin revelar si el correo existe (*prevención de Account Enumeration*). Posteriormente, el usuario podrá definir una nueva contraseña (`POST /api/reset-password`) consumiendo el token.
* **RF-04 — Control de Acceso Basado en Roles (RBAC):**
  El sistema debe restringir el acceso tanto a las vistas del frontend (`RoleGate`) como a los endpoints del backend (`auth.require_roles`) según el rol autenticado (`Administrador`, `Veterinario`, `Recepcionista`, `Cliente`), denegando con código `HTTP 403 Forbidden` cualquier operación no autorizada.

---

### 👥 Módulo 2: Administración de Usuarios del Sistema

* **RF-05 — Gestión de Usuarios (CRUD):**
  El sistema debe permitir exclusivamente al **Administrador** listar (`GET /api/usuarios`), crear (`POST /api/usuarios`), actualizar (`PUT /api/usuarios/{id}`) y eliminar (`DELETE /api/usuarios/{id}`) cuentas de usuario, asignando roles válidos y estado activo/inactivo. Por seguridad operativa, el sistema debe impedir que un administrador desactive o elimine su propia cuenta activa.

---

### 🐾 Módulo 3: Gestión de Clientes y Mascotas

* **RF-06 — Gestión del Padrón de Clientes:**
  El sistema debe permitir al personal (`Administrador`, `Veterinario`, `Recepcionista`) listar y registrar clientes (`GET/POST /api/clientes`) con DNI único, nombre, teléfono único, correo electrónico y, opcionalmente, credenciales de usuario para acceso al portal. La eliminación física de un cliente (`DELETE /api/clientes/{id}`) está restringida exclusivamente al `Administrador`.
* **RF-07 — Gestión de Mascotas y Fotografías:**
  El sistema debe permitir registrar mascotas asociadas a un cliente (`POST /api/clientes/{id}/mascotas`) con nombre, especie, raza, fecha de nacimiento, estado activo y fotografía. Tanto el personal como el cliente propietario (únicamente sobre sus propias mascotas y su propio perfil) podrán actualizar fotografías en formato Base64/URL (`PUT /api/mascotas/{id}/foto`, `PUT /api/clientes/me/foto`).

---

### 📅 Módulo 4: Agenda, Disponibilidad, Citas y Pagos de Servicios

* **RF-08 — Configuración de Horarios y Consulta de Disponibilidad:**
  El sistema debe permitir configurar los días laborables, hora de inicio, hora de fin y duración de turno en minutos (15, 30 o 60 min) (`GET/PUT /api/configuracion/horarios`). Con base en esta configuración y las citas activas, el sistema debe calcular y devolver los bloques horarios disponibles por fecha y veterinario (`GET /api/citas/disponibilidad`).
* **RF-09 — Agendamiento de Citas y Validación de Conflictos:**
  El sistema debe permitir agendar citas (`POST /api/citas`) indicando mascota, fecha y hora, motivo, tipo de servicio (*Consulta médica*, *Control / Seguimiento*, *Vacunación*, *Baño / Aseo*, *Examen auxiliar*, *Cirugía*) y veterinario opcional. El sistema debe:
  1. Verificar que no exista cruce de horario para el veterinario en el bloque de duración configurado (`HTTP 409 Conflict`).
  2. Asignar estado inicial `Programada` cuando la crea el personal, o `Pendiente` (sujeta a pago) cuando la solicita un `Cliente`.
  3. Crear automáticamente una orden de examen (`OrdenExamen` en estado `PENDIENTE`) si la cita es de tipo *Examen auxiliar*.
* **RF-10 — Reprogramación, Cambio de Estado y Cancelación de Citas:**
  El sistema debe permitir reprogramar la fecha/hora de una cita no cancelada (`PUT /api/citas/{id}/reprogramar`) validando disponibilidad, así como actualizar su estado (`PUT /api/citas/{id}/estado`, `PUT /api/citas/{id}/cancelar`). Para pasar una cita a estado `Cancelada`, el sistema debe exigir obligatoriamente el registro del `motivo_cancelacion`. La eliminación física de citas (`DELETE /api/citas/{id}`) solo está permitida para `Administrador` y `Veterinario`.
* **RF-11 — Tarifario y Pago de Citas con Emisión de Comprobante:**
  El sistema debe permitir al `Administrador` configurar los costos por tipo de cita y métodos de pago habilitados (`GET/PUT /api/configuracion/pago`). Los clientes o administradores podrán registrar y procesar pagos de citas (`POST /api/citas/{id}/pagos`, `POST /api/pagos/{id}/procesar`) mediante *YAPE*, *PLIN*, *TARJETA* o *EFECTIVO*. Para clientes, la tarifa a pagar se calculará de forma autoritativa en el backend desde la configuración. Al completarse el pago, la cita pasa automáticamente de `Pendiente` a `Programada` y se genera el comprobante electrónico correlativo (`BA64` para Boleta o `FA64` para Factura) mediante bloqueo pesimista en la secuencia (`ComprobanteService`). Si un pago ya se encuentra en estado `COMPLETADO`, el sistema rechazará reintentos de procesamiento (`400 Bad Request`) protegiendo la correlatividad fiscal.

---

### 🩺 Módulo 5: Atención Clínica, Historial Médico, Órdenes y Vacunación

* **RF-12 — Atención de Cita y Registro de Consulta Médica:**
  El sistema debe permitir al `Veterinario` y `Administrador` registrar consultas médicas directas (`POST /api/mascotas/{id}/consultas`) o atender una cita programada (`POST /api/citas/{id}/atender`) consignando diagnóstico, tratamiento y peso de la mascota. Al atender una cita, el sistema cambiará su estado a `Completada`, marcará como `REALIZADO` cualquier examen auxiliar asociado a la cita e impedirá que una misma cita sea atendida dos veces.
* **RF-13 — Emisión de Recetas, Órdenes de Exámenes Auxiliares y Referencias:**
  Durante el registro de una consulta, el sistema debe permitir emitir de forma integrada:
  1. **Recetas Médicas:** medicamento (vinculable a un producto del inventario), dosis, cantidad e indicaciones.
  2. **Órdenes de Exámenes Auxiliares:** selección de examen del catálogo activo, observaciones y registro/actualización posterior de resultados y estados (`PENDIENTE`, `REALIZADO`, `ANULADA` vía `PUT /api/examenes/{id}/resultado`).
  3. **Referencias Médicas:** derivación a un centro de destino, especialidad y motivo.
* **RF-14 — Visualización Integral del Historial Clínico:**
  El sistema debe consolidar y mostrar el historial médico completo de cada mascota (`GET /api/mascotas/{id}/historial`), incluyendo consultas, vacunas aplicadas, recetas emitidas, órdenes/resultados de exámenes auxiliares y referencias médicas, restringiendo a los usuarios con rol `Cliente` únicamente a la lectura de sus propias mascotas.
* **RF-15 — Registro de Vacunación y Alertas de Próximas Dosis:**
  El sistema debe permitir al `Veterinario` y `Administrador` registrar vacunas aplicadas (`POST /api/mascotas/{id}/vacunas`), calculando automáticamente la fecha de `proxima_dosis` si se indica un `intervalo_dias`, descontando 1 unidad del stock del almacén `Disponible` si se vincula un producto del inventario y registrando el movimiento correspondiente de tipo `VACUNA` en el Kardex. Asimismo, el sistema debe generar alertas automáticas de vacunas próximas a vencer en los siguientes **15 días** (`GET /api/recordatorios`).

---

### 📦 Módulo 6: Inventario, Catálogos, Almacenes y Compras (Ingresos)

* **RF-16 — Gestión de Catálogos Maestros (Proveedores, Categorías, Familias, Marcas y Exámenes):**
  El sistema debe permitir al `Administrador` gestionar (crear, editar, eliminar) y a la `Recepcionista` consultar los catálogos de **Proveedores** (`/api/proveedores` con RUC único, razón social, contacto, teléfono, dirección, email y moneda), **Categorías** (`/api/categorias`), **Familias** (`/api/familias`), **Marcas** (`/api/marcas`) y **Exámenes Auxiliares** (`/api/examenes-auxiliares`). Si un examen auxiliar ya tiene órdenes médicas registradas, su eliminación aplicará baja lógica (`activo = False`).
* **RF-17 — Gestión del Catálogo de Productos y Generación Automática de SKU:**
  El sistema debe permitir consultar productos (`GET /api/productos`), generar automáticamente el siguiente código SKU numérico correlativo (`GET /api/productos/next-sku`, serie `2026XXXX`) y permitir al `Administrador` crear, actualizar o eliminar productos con código de barras, imagen, descripción, precio de compra, precio de venta, stock actual, stock mínimo, condición de perecible, fecha de vencimiento y clasificación (proveedor, categoría, familia y marca).
* **RF-18 — Control de Stock por Almacén / Estado y Kardex de Movimientos:**
  El sistema debe gestionar el desglose de inventario por estados de almacén (`Disponible`, `No disponible`, `Dañado`, `Vencido`), permitiendo a `Administrador` y `Recepcionista`:
  1. Ajustar stock manualmente sumando o restando unidades (`PUT /api/productos/{id}/stock`).
  2. Cambiar el estado de almacén de un producto (`PUT /api/productos/{id}/estado-almacen`) o transferir cantidades parciales entre almacenes (`POST /api/almacenes/mover`).
  3. Registrar automáticamente cada operación en el historial de movimientos (`GET /api/movimientos-stock`) con fecha, usuario, tipo (`INGRESO`, `ANULACION_INGRESO`, `VENTA`, `AJUSTE`, `CAMBIO_ALMACEN`, `DEVOLUCION`, `ANULACION_VENTA`, `VACUNA`), cantidades y referencia.
* **RF-19 — Registro y Anulación de Ingresos de Mercadería (Compras a Proveedores):**
  El sistema debe permitir a `Administrador` y `Recepcionista` registrar compras/ingresos de mercadería (`GET/POST /api/ingresos`) indicando proveedor, modalidad de pago, tipo/serie/número de comprobante y el detalle de productos. El sistema validará que las fechas de vencimiento no sean pasadas, incrementará el stock del almacén `Disponible`, actualizará precios de compra/venta y registrará los movimientos de tipo `INGRESO`. Asimismo, el `Administrador` podrá eliminar/anular un ingreso (`DELETE /api/ingresos/{id}`), revirtiendo las cantidades ingresadas del stock `Disponible` y registrando los movimientos de tipo `ANULACION_INGRESO`.

---

### 🛒 Módulo 7: Punto de Venta (POS), Facturación, Devoluciones y Notas de Crédito

* **RF-20 — Registro de Ventas, Validación Atómica de Stock y Facturación:**
  El sistema debe permitir a `Administrador`, `Recepcionista` y `Cliente` registrar ventas (`POST /api/ventas`) cumpliendo las siguientes reglas de negocio:
  1. Verificar que cada producto tenga stock suficiente en el almacén `Disponible`, se encuentre en estado `Disponible` y no esté vencido.
  2. Aplicar de forma autoritativa en el backend los precios vigentes del catálogo (`Producto.precio`) cuando la solicitud es emitida por un `Cliente`. Para el personal en POS, se permite ajuste de precio unitario siempre que sea estrictamente positivo (`> 0`).
  3. Validar el documento fiscal según el comprobante: **DNI (8 dígitos)** o **Carnet de Extranjería (12 dígitos iniciando en 20)** para **Boleta** (serie `BA64`), y **RUC (11 dígitos)** para **Factura** (serie `FA64`).
  4. Descontar de forma atómica el stock, generar un código único de recojo en tienda (`SRB-XXXXXX`), asignar numeración correlativa de comprobante con bloqueo pesimista (`ComprobanteService`) y registrar los movimientos de salida (`VENTA`).
* **RF-21 — Gestión de Estados de Venta y Solicitud de Anulación:**
  El sistema debe controlar el flujo de estados de una venta (`Pendiente`, `Confirmada`, `Entregada`, `Anulación solicitada`, `Cancelada`):
  1. El `Cliente` puede solicitar la anulación de su compra (`POST /api/ventas/{id}/solicitar-anulacion`) siempre que aún no haya sido entregada.
  2. El personal (`Administrador`, `Recepcionista`) puede confirmar, marcar como entregada o cancelar la venta (`PUT /api/ventas/{id}/estado`). Al pasar a `Cancelada`, el sistema revierte automáticamente el stock al almacén `Disponible`.
* **RF-22 — Devoluciones Post-Entrega y Notas de Crédito:**
  El sistema debe permitir a `Administrador` y `Recepcionista` procesar devoluciones sobre ventas en estado `Entregada` (`POST /api/ventas/{id}/devolver`), revirtiendo el stock al almacén `Disponible` y eligiendo entre **Extorno** o emisión de **Nota de Crédito** (`NC-XXXXXX`, vigente por 180 días). El monto a devolver estará estrictamente limitado al total monetario efectivamente pagado en la venta (`venta.total`), rechazando montos negativos o superiores. Las Notas de Crédito activas con saldo disponible (`GET /api/notas-credito`) podrán ser aplicadas como descuento total o parcial en nuevas compras.

---

### 🌐 Módulo 8: Portal del Cliente y Canales Públicos

* **RF-23 — Portal de Autogestión del Cliente:**
  El sistema debe ofrecer un portal exclusivo para el rol `Cliente` (`/portal`) donde pueda gestionar su perfil, visualizar sus mascotas y su historial clínico completo, consultar sus recetas, órdenes de exámenes y referencias (`GET /api/ordenes-medicas/mias`), agendar/reprogramar/cancelar y pagar citas (mediante pasarela/modal integrado), revisar sus recordatorios de vacunación y consultar el estado de sus compras, pagos y notas de crédito. El frontend redirigirá automáticamente a los clientes que intenten acceder a rutas exclusivas del personal hacia `/portal`, y al personal que intente acceder a `/portal` hacia `/dashboard`.
* **RF-24 — Sitio Web Público, Blog y Catálogo de Tienda:**
  El sistema debe disponer de vistas públicas accesibles sin autenticación para la página institucional (`/`), el Blog informativo de cuidado animal (`/blog`, `/blog/:slug`) y el catálogo de productos de la Tienda (`/tienda`).

---

### 📊 Módulo 9: Dashboard, Reportes Analíticos y Auditoría

* **RF-25 — Dashboard Operativo y KPIs en Tiempo Real:**
  El sistema debe mostrar en el panel principal (`GET /api/stats`, `GET /api/citas/proximas`) indicadores clave en tiempo real: total de citas del día, citas/vacunas programadas pendientes, cantidad de productos con stock crítico (`stock <= stock_minimo`) y el listado de próximas citas.
* **RF-26 — Reportes Analíticos y Gráficos Estadísticos:**
  El sistema debe proveer a `Administrador` y `Recepcionista` reportes agregados filtrables por rango de fechas (`fecha_inicio`, `fecha_fin`):
  1. **Evolución de Ventas Diarias:** suma de ingresos agrupada por fecha (`GET /api/reportes/ventas`).
  2. **Ranking de Productos:** Top 10 de productos con mayor volumen de unidades vendidas (`GET /api/reportes/productos`).
  3. **Estadísticas de Citas por Mes:** conteo mensual desglosado por estado (`Completada`, `Cancelada`, `Programada`) (`GET /api/reportes/citas`).
  4. **Demografía de Mascotas:** distribución porcentual y absoluta de mascotas registradas por especie (`GET /api/reportes/demografia`).
* **RF-27 — Registro de Auditoría (Audit Log) Automatizado:**
  El sistema debe interceptar de forma transparente mediante un middleware HTTP todas las peticiones exitosas de mutación (`POST`, `PUT`, `DELETE`) sobre `/api/*`, así como los inicios de sesión (`LOGIN`), almacenando el usuario ejecutor, la acción, el endpoint afectado, la marca de tiempo (`timestamp`) y el detalle. La consulta del historial de auditoría (`GET /api/auditoria`) debe estar restringida únicamente al `Administrador`.

---

## 3. Matriz Resumen de Permisos por Rol (RBAC)

| Módulo / Funcionalidad | Administrador | Veterinario | Recepcionista | Cliente |
| :--- | :---: | :---: | :---: | :---: |
| **Autenticación y Recuperación de Contraseña** (`RF-01` a `RF-04`) | ✅ | ✅ | ✅ | ✅ |
| **Gestión de Usuarios del Sistema** (`RF-05`) | ✅ | ❌ | ❌ | ❌ |
| **Crear / Ver Clientes y Mascotas** (`RF-06`, `RF-07`) | ✅ | ✅ | ✅ | 🔒 *(Solo propios)* |
| **Eliminar Clientes Físicamente** (`RF-06`) | ✅ | ❌ | ❌ | ❌ |
| **Configurar Horarios de Atención** (`RF-08`) | ✅ | ✅ | ✅ | ❌ *(Solo lectura)* |
| **Configurar Tarifas y Métodos de Pago de Citas** (`RF-11`) | ✅ | ❌ *(Solo lectura)* | ❌ *(Solo lectura)* | ❌ *(Solo lectura)* |
| **Agendar / Reprogramar / Cancelar Citas** (`RF-09`, `RF-10`) | ✅ | ✅ | ✅ | 🔒 *(Solo propias)* |
| **Eliminar Citas Físicamente** (`RF-10`) | ✅ | ✅ | ❌ | ❌ |
| **Atender Citas, Registrar Consultas, Recetas, Exámenes y Vacunas** (`RF-12` a `RF-15`) | ✅ | ✅ | ❌ | ❌ *(Solo lectura)* |
| **Crear / Editar / Eliminar Productos, Proveedores y Catálogos** (`RF-16`, `RF-17`) | ✅ | ❌ *(Lectura prod.)* | ❌ *(Lectura)* | ❌ *(Lectura prod.)* |
| **Ajustar Stock, Mover Almacenes y Registrar Ingresos de Compra** (`RF-18`, `RF-19`) | ✅ | ❌ | ✅ | ❌ |
| **Registrar Ventas (POS / Tienda) y Usar Notas de Crédito** (`RF-20`) | ✅ | ❌ *(Solo lectura)* | ✅ | 🔒 *(Compra propia)* |
| **Confirmar / Entregar / Anular Ventas y Procesar Devoluciones** (`RF-21`, `RF-22`) | ✅ | ❌ | ✅ | 🔒 *(Solicitar anulación)* |
| **Ver Reportes Analíticos y Estadísticos** (`RF-26`) | ✅ | ❌ | ✅ | ❌ |
| **Consultar Registro de Auditoría (Audit Log)** (`RF-27`) | ✅ | ❌ | ❌ | ❌ |
