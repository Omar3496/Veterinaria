# Colección de Pruebas API en Bruno - Sirbull Pet Clinic

Esta colección de [Bruno](https://www.usebruno.com/) está diseñada para la **presentación del proyecto backend**, permitiendo demostrar de forma ordenada y automatizada las peticiones HTTP fundamentales:
- **POST** (Autenticación JWT, RBAC y Creación de recursos)
- **GET** (Consultas públicas y protegidas)
- **PUT** (Actualizaciones completas)
- **PATCH** (Actualizaciones parciales)
- **DELETE** (Eliminaciones con confirmación)

---

## 🚀 Guía Rápida para la Presentación

### 1. Iniciar el Backend
Asegúrate de que el servidor Spring Boot esté ejecutándose en el puerto `8081`:
```powershell
cd Veterinaria
.\mvnw.cmd spring-boot:run
```

### 2. Abrir la Colección en Bruno
1. Abre **Bruno**.
2. Haz clic en **Open Collection** (Abrir Colección).
3. Selecciona la carpeta `bruno` de este repositorio (`proyecto/bruno`).
4. En la esquina superior derecha de Bruno, selecciona el entorno **`Local`** (apunta a `http://localhost:8081`).

---

## 🔐 Control de Acceso Basado en Roles (RBAC)

Para demostrar en tu presentación que la seguridad de Spring Boot está activa y rechaza a usuarios no autorizados:

1. **`01. Login como Administrador`**: Obtiene el token de Admin y lo guarda en `{{token}}` y `{{admin_token}}`.
2. **`02. Login como Cliente`**: Obtiene el token de Cliente (`jperez`) y lo guarda en `{{token}}` y `{{cliente_token}}`.
3. **`03. Demostración RBAC - Cliente Denegado (403 Forbidden)`**: Envía una petición a `GET /api/clientes` con el token de Cliente. El backend responde **`403 Forbidden`** con el mensaje `"No tienes permisos suficientes para realizar esta acción"`.
4. **`04. Demostración RBAC - Cliente Consulta su Perfil (200 OK)`**: Demuestra lo que el cliente sí puede consultar (`GET /api/clientes/me`), retornando **`200 OK`**.
5. **`05. Reconectar como Administrador`**: Restablece `{{token}}` al rol de Administrador para que el resto de operaciones CRUD puedan ejecutarse con normalidad.
6. **`03. Login Personalizado`**: Solicita interactivamente `username` y `password` en una ventana modal (Prompt) para autenticarte con cualquier usuario del sistema y guardar su token en `{{token}}`.

### 👥 Usuarios de Prueba Precargados (DataSeeder)
| Usuario | Contraseña | Rol | Permisos Principales |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `Administrador` | Acceso total (CRUD, auditoría, usuarios, catálogos, citas) |
| `vet` | `vet123` | `Veterinario` | Citas, consultas, historias clínicas, recetas, inventario |
| `recep` | `recep123` | `Recepcionista` | Clientes, citas, pagos, ventas, productos |
| `jperez` | `cliente123` | `Cliente` | Portal cliente: citas propias (`/mias`), perfil propio (`/me`) |
| `mlopez` | `cliente123` | `Cliente` | Portal cliente: citas propias (`/mias`), perfil propio (`/me`) |

---

## 🔄 Flujo de Ejecución Recomendado (Ciclo de Vida)

| Orden | Carpeta / Petición | Método | Endpoint | Descripción |
| :---: | :--- | :---: | :--- | :--- |
| **1** | `00-Auth / 01. Login como Administrador` | `POST` | `/token` | Obtiene JWT de `admin` (`{{admin_token}}`). |
| **2** | `00-Auth / 02. Login como Cliente` | `POST` | `/token` | Obtiene JWT de `jperez` (`{{cliente_token}}`). |
| **3** | `00-Auth / 03. Cliente Denegado (403)` | `GET` | `/api/clientes` | **Demuestra rechazo 403 Forbidden** para rol Cliente. |
| **4** | `00-Auth / 04. Cliente Perfil Propio (200)` | `GET` | `/api/clientes/me` | Demuestra acceso 200 OK permitido para perfil propio. |
| **5** | `00-Auth / 05. Reconectar como Administrador` | `POST` | `/token` | Restablece `{{token}}` como Administrador para el CRUD. |
| **6** | `01-GET / 01. Health Check` | `GET` | `/` | Comprueba que el API está en línea (público). |
| **7** | `01-GET / 02. Listar Productos Públicos` | `GET` | `/api/productos` | Catálogo público paginado (`skip=0&limit=10`). |
| **8** | `01-GET / 03. Listar Categorías` | `GET` | `/api/categorias` | Consulta protegida con Bearer Token. |
| **9** | `01-GET / 04. Listar Clientes` | `GET` | `/api/clientes` | Consulta protegida de clientes registrados (Admin/Staff). |
| **10** | `01-GET / 05. Listar Citas` | `GET` | `/api/citas` | Consulta protegida de citas de la clínica. |
| **11** | `01-GET / 06. Obtener Cliente por ID` | `GET` | `/api/clientes/1` | Consulta detalle de un cliente específico por ID. |
| **12** | `01-GET / 07. Obtener Mascota por ID` | `GET` | `/api/mascotas/1` | Consulta detalle de una mascota específica por ID. |
| **13** | `01-GET / 08. Obtener Cita por ID` | `GET` | `/api/citas/1` | Consulta detalle de una cita específica por ID. |
| **14** | `01-GET / 09. Obtener Producto por ID` | `GET` | `/api/productos/1` | Consulta detalle de un producto específico por ID (público). |
| **15** | `01-GET / 10. Obtener Categoría por ID` | `GET` | `/api/categorias/1` | Consulta detalle de una categoría específica por ID. |
| **16** | `02-POST / 01. Crear Categoría` | `POST` | `/api/categorias` | Crea `"Accesorios y Medicinas Demo"`, captura su nuevo ID en `{{categoria_id}}`. |
| **17** | `02-POST / 02. Crear Cliente` | `POST` | `/api/clientes` | Registra un nuevo cliente con DNI y teléfono, guardando `{{cliente_id}}`. |
| **18** | `03-PUT / 01. Actualizar Categoría Completa` | `PUT` | `/api/categorias/{{categoria_id}}` | Reemplaza el recurso con `"Farmacia Veterinaria y Medicinas"`. |
| **19** | `03-PUT / 02. Actualizar Horarios de Atención` | `PUT` | `/api/configuracion/horarios` | Actualiza rangos de atención del establecimiento. |
| **20** | `04-PATCH / 01. Actualización Parcial Categoría` | `PATCH` | `/api/categorias/{{categoria_id}}` | Aplica una modificación parcial `"Farmacia y Medicinas Premium (PATCH)"`. |
| **21** | `04-PATCH / 02. Actualizar Estado de Cita` | `PATCH` | `/api/citas/{{cita_id}}/estado?estado=EN_ATENCION` | Modifica únicamente el estado de la cita médica a `EN_ATENCION`. |
| **22** | `05-DELETE / 01. Eliminar Categoría` | `DELETE` | `/api/categorias/{{categoria_id}}` | Elimina la categoría creada y verifica respuesta 200. |
| **23** | `05-DELETE / 02. Eliminar Cliente` | `DELETE` | `/api/clientes/{{cliente_id}}` | Elimina el cliente creado y verifica respuesta 200. |

---

## 📌 Regla de Sintaxis `.bru` (Validada y Respetada)

En todos los archivos `.bru`, los bloques multilínea (`tests { ... }`, `body:json { ... }`, `script:post-response { ... }`):
- Tienen sus llaves internas y llamadas (`  });`, `  {`, `  }`) con **sangría de 2 o 4 espacios**.
- La única llave de cierre `}` que se ubica en la **columna 1 (sin sangría)** es la que cierra el bloque principal.
- Esto evita el error de parseo `Expected end of input` de Bruno.
