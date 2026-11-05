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
| **11** | `02-POST / 01. Crear Categoría` | `POST` | `/api/categorias` | Crea `"Accesorios y Medicinas Demo"`, captura su nuevo ID en `{{categoria_id}}`. |
| **12** | `02-POST / 02. Crear Cliente` | `POST` | `/api/clientes` | Registra un nuevo cliente con DNI y teléfono, guardando `{{cliente_id}}`. |
| **13** | `03-PUT / 01. Actualizar Categoría Completa` | `PUT` | `/api/categorias/{{categoria_id}}` | Reemplaza el recurso con `"Farmacia Veterinaria y Medicinas"`. |
| **14** | `03-PUT / 02. Actualizar Horarios de Atención` | `PUT` | `/api/configuracion/horarios` | Actualiza rangos de atención del establecimiento. |
| **15** | `04-PATCH / 01. Actualización Parcial Categoría` | `PATCH` | `/api/categorias/{{categoria_id}}` | Aplica una modificación parcial `"Farmacia y Medicinas Premium (PATCH)"`. |
| **16** | `04-PATCH / 02. Actualizar Estado de Cita` | `PATCH` | `/api/citas/{{cita_id}}/estado?estado=EN_ATENCION` | Modifica únicamente el estado de la cita médica a `EN_ATENCION`. |
| **17** | `05-DELETE / 01. Eliminar Categoría` | `DELETE` | `/api/categorias/{{categoria_id}}` | Elimina la categoría creada y verifica respuesta 200. |
| **18** | `05-DELETE / 02. Eliminar Cliente` | `DELETE` | `/api/clientes/{{cliente_id}}` | Elimina el cliente creado y verifica respuesta 200. |

---

## 📌 Regla de Sintaxis `.bru` (Validada y Respetada)

En todos los archivos `.bru`, los bloques multilínea (`tests { ... }`, `body:json { ... }`, `script:post-response { ... }`):
- Tienen sus llaves internas y llamadas (`  });`, `  {`, `  }`) con **sangría de 2 o 4 espacios**.
- La única llave de cierre `}` que se ubica en la **columna 1 (sin sangría)** es la que cierra el bloque principal.
- Esto evita el error de parseo `Expected end of input` de Bruno.
