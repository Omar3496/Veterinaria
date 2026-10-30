# 🛡️ Especificación de Requerimientos No Funcionales (RNF) — Sirbull Pet Clinic

Este documento define los **Requerimientos No Funcionales (RNF)** del **Sistema Integral de Gestión Veterinaria Sirbull Pet Clinic (`com.tecsolutions.Veterinaria`)**, clasificados según los atributos de calidad del estándar **ISO/IEC 25010** (Seguridad, Fiabilidad, Rendimiento, Usabilidad, Mantenibilidad y Portabilidad/DevOps) y alineados con la arquitectura completa del proyecto (**Backend en Java 25 con Spring Boot 4.1.1** y **Frontend en React 19 con Vite y TailwindCSS**).

---

## 1. Seguridad y Criptografía (Security)

* **RNF-01 — Autenticación Stateless con JWT, RBAC en Spring Security y Hashing Criptográfico de Contraseñas:**
  El sistema no debe almacenar contraseñas en texto plano bajo ninguna circunstancia. Todas las credenciales de la entidad `Usuario` (`usuarios.hashed_password`) deben cifrarse mediante **Spring Security** (`spring-boot-starter-security`) utilizando el algoritmo **BCrypt** (`BCryptPasswordEncoder`) con generación de *salt* aleatorio. La gestión de sesiones debe ser *stateless* (`SessionCreationPolicy.STATELESS`) mediante JSON Web Tokens (**JWT**) firmados con `HmacSHA256` del JDK (`javax.crypto.Mac` + `Base64Url`, sin librerías externas adicionales en `pom.xml`), con un tiempo de expiración predeterminado de 24 horas (`1440` minutos) y clave secreta cargada desde variables de entorno (`SECRET_KEY` / `jwt.secret`). El endpoint `POST /token` (`application/x-www-form-urlencoded`) emitirá `{ "access_token": "<jwt>" }` con los claims `sub` (username) y `rol`, y el filtro `OncePerRequestFilter` verificará que la cuenta esté activa (`isActive = true`) y hará cumplir el Control de Acceso Basado en Roles (**RBAC** según `RolEnum`: `Administrador`, `Veterinario`, `Recepcionista`, `Cliente`), denegando con `HTTP 403 Forbidden` cualquier operación no autorizada.
* **RNF-02 — Protección contra Ataques de Fuerza Bruta y Enumeración de Cuentas:**
  El endpoint de inicio de sesión (`POST /token`) debe contar con un limitador de velocidad (*Rate Limiting* mediante un filtro `OncePerRequestFilter` en memoria en Spring Boot) que restrinja las peticiones a un máximo de **5 intentos por minuto por dirección IP**. Asimismo, el flujo de recuperación de contraseña (`/api/recuperar-password` y `/api/reset-password`) debe almacenar en la entidad `PasswordResetToken` (`password_reset_tokens`) únicamente el hash **SHA-256** (`MessageDigest`) del token temporal (`tokenHash`, con expiración estricta `expiresAt` de 30 minutos y marcado de uso `used = true`) y responder con un mensaje uniforme exista o no el correo consultado (*prevención de Account Enumeration*).
* **RNF-03 — Cabeceras HTTP de Seguridad (Hardening OWASP) y Política CORS Restrictiva:**
  Tanto la cadena de filtros de seguridad del backend (`SecurityFilterChain` en **Spring Security**) como el proxy inverso (`nginx.conf`) deben inyectar en todas las respuestas las siguientes cabeceras de seguridad:
  * `Strict-Transport-Security: max-age=63072000; includeSubDomains; preload` (HSTS)
  * `X-Content-Type-Options: nosniff`
  * `X-Frame-Options: DENY` (Prevención de *Clickjacking*; permitiendo `SAMEORIGIN` exclusivamente para `/h2-console` en entorno local de desarrollo)
  * `Content-Security-Policy: default-src 'self'; img-src 'self' https: data:; style-src 'self' 'unsafe-inline'; script-src 'self'`
  * Configuración **CORS** (`CorsConfigurationSource` / `@CrossOrigin`) restringida exclusivamente a los dominios autorizados en la variable de entorno `CORS_ORIGINS` (sin uso de comodín `*`).

---

## 2. Fiabilidad, Integridad y Recuperación (Reliability & Data Integrity)

* **RNF-04 — Respaldo Automatizado, Sembrado Idempotente y Política de Retención de Base de Datos:**
  El sistema debe proveer un componente de sembrado inicial idempotente (`DataSeeder` implementando `CommandLineRunner`) y un mecanismo automatizable de copias de seguridad (tarea programada `@Scheduled` en Spring Boot / rutina de backup con `SCRIPT TO` en H2 o `pg_dump` en PostgreSQL) que genere respaldos íntegros de la base de datos con marca de tiempo (`veterinaria_backup_YYYYMMDD_HHMMSS.sql`) y ejecute una política de rotación automática que elimine respaldos antiguos que superen el período de retención configurado (`BACKUP_RETENTION_DAYS`, por defecto **30 días**).
* **RNF-05 — Atomicidad Transaccional y Consistencia de Datos (ACID):**
  Todas las operaciones clínicas, financieras y logísticas críticas (atención de citas con generación integrada de `Consulta`, `RecetaMedica`, `OrdenExamen` y `ReferenciaMedica`; aplicación de `Vacuna` con descuento de inventario; registro de ventas en `Venta` y `DetalleVenta` con descuento de stock en `StockAlmacen`, numeración correlativa en `ComprobanteSeq` y registro de kardex en `MovimientoStock`; recepción de compras a proveedores en `Ingreso` e `IngresoDetalle`; traslados entre almacenes; registro de `Devolucion`; y emisión/aplicación de `NotaCredito`) deben ejecutarse de manera atómica mediante `@Transactional` de **Spring Data JPA**. Asimismo, las relaciones maestro-detalle (`Cliente -> Mascota -> Cita`, `Consulta -> RecetaMedica / ReferenciaMedica`, `Producto -> StockAlmacen`, `Ingreso -> IngresoDetalle`, `Venta -> DetalleVenta`) deben garantizar integridad en cascada (`CascadeType.ALL`, `orphanRemoval = true`). Ante cualquier error de validación (stock insuficiente, producto vencido, RUC/DNI inválido o cruce de horario `HTTP 409 Conflict`) o excepción en mitad del proceso, el gestor transaccional debe ejecutar `rollback` inmediato de la sesión de base de datos para garantizar que no existan descuentos parciales ni descuadres de inventario.
* **RNF-06 — Alta Disponibilidad, Autorecuperación y Monitoreo de Salud (Healthchecks):**
  Los servicios contenerizados deben configurarse con políticas de reinicio automático (`restart: always`) y chequeos de salud periódicos (`HEALTHCHECK`):
  * **Backend (Spring Boot):** verificación HTTP cada 30 segundos con timeout de 3 segundos en el puerto `8081` (`curl -f http://localhost:8081/actuator/health` o `http://localhost:8081/`).
  * **Base de Datos (H2 / PostgreSQL):** disponibilidad inmediata en memoria (`jdbc:h2:mem:idat_proyecto_db`) en desarrollo/testing y verificación de disponibilidad con `pg_isready` cada 5 segundos antes de iniciar el servicio backend en producción (`condition: service_healthy`).

---

## 3. Rendimiento y Eficiencia (Performance & Scalability)

* **RNF-07 — Baja Latencia en Peticiones REST (Arquitectura Spring Boot WebMVC sobre Java 25):**
  El backend, construido sobre **Spring Boot 4.1.1 (`spring-boot-starter-webmvc`)** y ejecutado mediante **Apache Tomcat embebido** sobre **Java 25** (puerto `8081`), debe procesar las operaciones transaccionales y consultas CRUD estándar en un tiempo de respuesta promedio inferior a **200 ms** en red local, y las consultas analíticas de agregación JPQL/SQL para el Dashboard y Reportes (`/api/stats`, `/api/reportes/ventas`, `/api/reportes/productos`, `/api/reportes/citas`, `/api/reportes/demografia`) en menos de **500 ms**.
* **RNF-08 — Indexación y Paginación Nativa en Base de Datos (JPA / Hibernate):**
  Los campos de búsqueda frecuente, claves de negocio y restricciones de unicidad del modelo de dominio (`usuarios.username`, `clientes.dni`, `clientes.telefono`, `mascotas.nombre`, `examenes_auxiliares.nombre`, `proveedores.ruc`, `categorias.nombre`, `familias.nombre`, `marcas.nombre`, `productos.sku`, `productos_stock_almacen.producto_id`, `ventas.codigo_recojo`, `notas_credito.codigo`, `comprobantes_seq.serie`, `configuracion.clave`, `password_reset_tokens.token_hash`) deben estar indexados en el motor relacional mediante anotaciones JPA (`@Table(indexes = @Index(...))` y `@Column(unique = true)`). Además, los repositorios (`JpaRepository`) y endpoints de listado general deben procesar la paginación de forma nativa en SQL mediante `Pageable` (`PageRequest.of(...)`), prohibiendo el filtrado y paginado en memoria JVM (`.stream().skip().limit()`) para evitar sobrecarga de RAM y degradación de rendimiento.
* **RNF-09 — Entrega Estática Optimizada y Proxy Inverso con Nginx:**
  El frontend debe compilarse mediante **Vite** generando paquetes (*bundles*) minificados y servirse directamente desde **Nginx Alpine**, el cual actúa simultáneamente como servidor de activos estáticos de alto rendimiento y como *Reverse Proxy* hacia el backend Spring Boot (`http://backend:8081`) para las rutas `/api/` y `/token`.

---

## 4. Usabilidad y Experiencia de Usuario (Usability & UX)

* **RNF-10 — Diseño Web Responsivo y Adaptable (Responsive Design):**
  La interfaz gráfica (desarrollada en **React 19** y **TailwindCSS**) debe ser completamente responsiva, adaptando su disposición visual a monitores de escritorio (administración y POS), tabletas (consultorio veterinario) y teléfonos móviles (Portal del Cliente `/portal`, con barra superior móvil y menú lateral deslizable).
* **RNF-11 — Arquitectura SPA, Transiciones Fluidas y Retroalimentación Inmediata:**
  El frontend debe comportarse como una *Single Page Application* (SPA) gestionada con `react-router-dom` y estado global persistente en **Zustand**, ofreciendo transiciones suaves entre vistas (`PageTransition`), notificaciones visuales inmediatas tipo *Toast* (`ToastProvider`) y mensajes descriptivos en idioma español ante errores de validación y negocio devueltos por el backend (`@RestControllerAdvice` / `ResponseEntity` con códigos `HTTP 400/403/404/409/422`). El manejador global de excepciones (`GlobalExceptionHandler`) debe enmascarar los errores no controlados devolviendo mensajes genéricos seguros sin exponer detalles internos de la base de datos o trazas de excepción.
* **RNF-12 — Generación de Documentos Imprimibles y Exportables:**
  Los comprobantes electrónicos (Boletas `BA64` y Facturas `FA64` gestionados para `Venta` y `Pago`), tickets de venta con código de recojo (`SRB-XXXXXX`), recetas médicas, órdenes de exámenes auxiliares y reportes/historiales clínicos deben presentar un diseño estructurado compatible con la exportación a formato PDF e impresión directa desde el navegador.

---

## 5. Mantenibilidad, Calidad de Código y Trazabilidad (Maintainability & Auditability)

* **RNF-13 — Desacoplamiento con Patrón DTO, Mappers (`@Component`), Lombok y Serialización `SNAKE_CASE` Compatible con el Frontend:**
  El 100% de las cargas útiles (*payloads*) de entrada y respuestas de la API deben desacoplarse de las entidades JPA y validarse mediante **Data Transfer Objects (DTOs)**, mappers dedicados en Java puro (`@Component` en `com.tecsolutions.Veterinaria.mapper` sin dependencias externas adicionales) y lógica de servicio (incluyendo validación de DNI de 8 dígitos, CE de 12 dígitos iniciando en `20` y RUC de 11 dígitos), garantizando que nunca se filtren campos sensibles de las entidades (como `hashedPassword`, `tokenHash` o relaciones internas no autorizadas). Se mantendrán convenciones `camelCase` idiomáticas en Java junto con `spring.jackson.property-naming-strategy=SNAKE_CASE` en `application.properties` y **Project Lombok** (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`). Los DTOs y propiedades calculadas (`@JsonProperty(access = JsonProperty.Access.READ_ONLY)`) expondrán en JSON los atributos en `snake_case` esperados por el frontend (`veterinario_nombre`, `examen_auxiliar_nombre`, `cita_motivo`, `producto_nombre`, `proveedor_nombre`, `categoria_nombre`, `familia_nombre`, `marca_nombre`, `estado_almacen_nombre`, `stock_por_almacen`, `usuario_nombre`, `cliente_nombre`, `vendedor_nombre`, `comprobante` y `vigente`) sin referencias circulares, y los errores de negocio devolverán `{ "detail": "<mensaje>" }` mediante `@RestControllerAdvice`.
* **RNF-14 — Integración Continua (CI/CD) y Suite de Pruebas Automatizadas con Spring Boot Test:**
  El proyecto debe contar con una suite de pruebas automatizadas en **JUnit 5, MockMvc y Spring Boot Test** (`src/test/java/com/tecsolutions/Veterinaria/`, empleando `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test` y `spring-boot-starter-security-test`) que valide autenticación, RBAC (`RolEnum`), CRUDs, configuración de horarios (`Configuracion`), citas y pagos (`Pago`), consultas clínicas (`Consulta`), órdenes médicas (`RecetaMedica`, `OrdenExamen`, `ReferenciaMedica`), inventario multi-almacén (`Producto`, `StockAlmacen`, `MovimientoStock`), compras (`Ingreso`), vacunas y recordatorios (`Vacuna`), ventas POS (`Venta`, `Devolucion`, `NotaCredito`) y portal del cliente. El pipeline de **GitHub Actions** (`.github/workflows/ci.yml`) debe ejecutar automáticamente en cada `push` o `pull_request` a `main`:
  1. Ejecución de pruebas unitarias e integración del backend con Maven Wrapper en **Java 25** (`./mvnw test`).
  2. Compilación de verificación del frontend en Node.js 20 (`npm run build`).
  3. Construcción íntegra de los contenedores (`docker-compose build`).
* **RNF-15 — Trazabilidad y No Repudio mediante Auditoría Desacoplada (`Auditoria`):**
  El registro de auditoría (`Audit Log`, persistido en la entidad `Auditoria` / tabla `auditorias` con `usuario_id`, `accion`, `endpoint`, `resource_id`, `timestamp` y `detalle`) debe operar mediante un interceptor o filtro HTTP de Spring (`HandlerInterceptor` / `OncePerRequestFilter`) desacoplado de la lógica de negocio de cada controlador, garantizando que toda acción `POST`, `PUT`, `DELETE` exitosa sobre `/api/*` y cada inicio de sesión (`LOGIN`) quede registrado con usuario, fecha/hora exacta, ruta y acción, restringiendo su consulta (`GET /api/auditoria`) exclusivamente al rol `Administrador`.

---

## 6. Portabilidad, Despliegue e Infraestructura (Portability & DevOps)

* **RNF-16 — Contenerización Multi-Etapa y Ejecución sin Privilegios (Non-Root):**
  La plataforma debe empaquetarse en contenedores **Docker** orquestados con **Docker Compose** (`docker-compose.yml`), permitiendo levantar todo el ecosistema con un solo comando (`docker-compose up --build -d`). Por seguridad de infraestructura, el contenedor del backend (construido en multi-etapa con Maven y `eclipse-temurin:25-jre-alpine`) debe ejecutarse bajo un usuario de sistema sin privilegios de superusuario (`USER appuser`).
* **RNF-17 — Portabilidad de Base de Datos (H2 / PostgreSQL) y Autoevolución de Esquema con Hibernate:**
  El acceso a datos de las 28 entidades del dominio (`Usuario`, `Cliente`, `Mascota`, `Cita`, `Consulta`, `ExamenAuxiliar`, `RecetaMedica`, `OrdenExamen`, `ReferenciaMedica`, `Vacuna`, `Proveedor`, `Categoria`, `Familia`, `Marca`, `Producto`, `StockAlmacen`, `MovimientoStock`, `Ingreso`, `IngresoDetalle`, `Venta`, `DetalleVenta`, `NotaCredito`, `Devolucion`, `ComprobanteSeq`, `Auditoria`, `Configuracion`, `Pago`, `PasswordResetToken`) debe abstraerse mediante **Spring Data JPA e Hibernate** (`spring-boot-starter-data-jpa`), permitiendo operar de forma transparente con **H2 Database en memoria** (`jdbc:h2:mem:idat_proyecto_db`, dialecto `H2Dialect` y consola en `/h2-console`) en entornos locales/testing y con **PostgreSQL 15 Alpine** en producción mediante la variable de entorno `SPRING_DATASOURCE_URL` / `DB_URL`. Además, el sistema debe ejecutar rutinas idempotentes de actualización y migración de esquema al iniciar (`spring.jpa.hibernate.ddl-auto=update` / Flyway y `DataSeeder`) sin pérdida de datos existentes.

---

## 7. Matriz Resumen de Requerimientos No Funcionales

| Código | Categoría (ISO/IEC 25010) | Atributo Clave | Métrica / Mecanismo de Verificación |
| :--- | :--- | :--- | :--- |
| **RNF-01** | Seguridad | Spring Security + JWT + `BCrypt` | Tokens `HmacSHA256` JDK (exp. 24h), `RolEnum` RBAC y `BCryptPasswordEncoder` |
| **RNF-02** | Seguridad | Anti-Brute Force & Reset Seguro | Rate limit `5/min` en `/token`; `PasswordResetToken` SHA-256 (30 min) |
| **RNF-03** | Seguridad | Cabeceras OWASP y CORS | HSTS, CSP, `X-Frame-Options: DENY`, `nosniff`, CORS explícito en Spring |
| **RNF-04** | Fiabilidad | Copias de Seguridad y `DataSeeder` | Backup automatizado con timestamp, purga a 30 días y semilla idempotente |
| **RNF-05** | Fiabilidad | Transacciones ACID (`@Transactional`) | Rollback automático en ventas, stock, compras, citas, vacunas y notas |
| **RNF-06** | Fiabilidad | Disponibilidad y Healthchecks | `HEALTHCHECK` cada 30s (API `:8081`) y 5s (PostgreSQL) + `restart: always` |
| **RNF-07** | Rendimiento | Tiempo de Respuesta API (Java 25) | `< 200 ms` en operaciones CRUD y `< 500 ms` en `/api/reportes/*` |
| **RNF-08** | Rendimiento | Indexación JPA y Paginación | Índices `@Index` en las 16 columnas clave y paginación `skip`/`limit` (100 reg.) |
| **RNF-09** | Rendimiento | Entrega Estática y Reverse Proxy | Build estático en Vite servido por Nginx Alpine hacia `:8081` |
| **RNF-10** | Usabilidad | Diseño Responsivo | Adaptación móvil (`/portal`), tablet y desktop con React 19 y TailwindCSS |
| **RNF-11** | Usabilidad | Experiencia SPA y Feedback | Navegación sin recarga, Zustand, animaciones y alertas *Toast* (`400/404/409/422`) |
| **RNF-12** | Usabilidad | Impresión y Exportación | Formato imprimible/PDF para boletas `BA64`, facturas `FA64` e historial clínico |
| **RNF-13** | Mantenibilidad | Patrón DTO, Lombok y `SNAKE_CASE` | Validación en DTO/servicio, `spring.jackson.property-naming-strategy=SNAKE_CASE`, Lombok y `{ "detail" }` |
| **RNF-14** | Mantenibilidad | Calidad y CI/CD Automatizado | Pipeline GitHub Actions (`./mvnw test` + `vite build` + `docker-compose build`) |
| **RNF-15** | Mantenibilidad | Trazabilidad / Audit Log (`Auditoria`) | Interceptor Spring automático para `POST/PUT/DELETE/LOGIN` en tabla `auditorias` |
| **RNF-16** | Portabilidad | Contenedores Seguros (JRE 25) | Docker multi-stage (Maven + Temurin 25) y ejecución no-root (`USER appuser`) |
| **RNF-17** | Portabilidad | Motor BD Dual y Migración JPA | Soporte H2 / PostgreSQL 15 para las 28 entidades vía `ddl-auto=update` |
