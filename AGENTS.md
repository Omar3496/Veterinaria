# Sirbull Pet Clinic (`Veterinaria`)

## Agent skills

Before writing or changing any code, read and follow:
- `.agents/skills/ponytail/SKILL.md`
- `.agents/skills/karpathy-guidelines/SKILL.md`

This applies to every coding task in this repo.

### Issue tracker

Local markdown files under `.scratch/<feature-slug>/`. See `docs/agents/issue-tracker.md`.

### Triage labels

Default five canonical triage roles (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context (`GLOSSARY.md`, `docs/adr/`, `REQUERIMIENTOS_FUNCIONALES.md`, and `REQUERIMIENTOS_NO_FUNCIONALES.md` at the repo root). Whenever a functional requirement (`RF-01..RF-27`) or non-functional/architectural decision (`RNF-01..RNF-17`) changes during grilling, planning, or coding, update `REQUERIMIENTOS_FUNCIONALES.md` / `REQUERIMIENTOS_NO_FUNCIONALES.md` inline alongside `GLOSSARY.md` and `docs/adr/`. See `docs/agents/domain.md`.

## 1. Project Overview

Monorepo for **Sirbull Pet Clinic**, an integrated veterinary clinic management, multi-warehouse inventory, POS/billing (`Boleta` `BA64` / `Factura` `FA64`), and customer self-service portal system (`Administrador`, `Veterinario`, `Recepcionista`, `Cliente`).

* **Active Port (Python → Java)**: `./frontend` is complete and was built against a legacy Python/FastAPI backend. `./Veterinaria` is the target Spring Boot backend port.
* **Canonical Spec & Schema**:
  * Functional & non-functional specs: `REQUERIMIENTOS_FUNCIONALES.md` (RF-01..RF-27) and `REQUERIMIENTOS_NO_FUNCIONALES.md` (RNF-01..RNF-17).
  * Canonical 28-entity schema reference: `models.py` at the repo root (`Usuario`, `Cliente`, `Mascota`, `Cita`, `Consulta`, `ExamenAuxiliar`, `RecetaMedica`, `OrdenExamen`, `ReferenciaMedica`, `Vacuna`, `Proveedor`, `Categoria`, `Familia`, `Marca`, `Producto`, `StockAlmacen`, `MovimientoStock`, `Ingreso`, `IngresoDetalle`, `Venta`, `DetalleVenta`, `NotaCredito`, `Devolucion`, `ComprobanteSeq`, `Auditoria`, `Configuracion`, `Pago`, `PasswordResetToken`).
* **Migration Gotchas**:
  * **Throwaway starter classes, permanent package layout**: Keep the `com.tecsolutions.Veterinaria` package structure (`config`, `controller`, `dto`, `model`, `repository`, `services`), but replace the 4 initial trial classes (`Propietario`, `Veterinario`, simplified `Mascota`/`Cita`) with the 28-entity domain (`Cliente` instead of `Propietario`; `Usuario` with `RolEnum` instead of a standalone `Veterinario` entity).
  * **Wire compatibility**: Keep Java code in idiomatic `camelCase` and set `spring.jackson.property-naming-strategy=SNAKE_CASE` in `application.properties` so JSON matches `./frontend`. Serve `POST /token` (`application/x-www-form-urlencoded`) returning `{ "access_token": "<jwt>" }` (`sub`, `rol` claims) and `{ "detail": "<message>" }` error payloads. See `docs/adr/0001-preserve-fastapi-wire-contract.md`.
  * **Port alignment**: Spring Boot runs on `:8081` (`application.properties`, `RNF-06`/`RNF-07`). `./frontend/src/api/axiosConfig.js`, `./frontend/vite.config.js`, and `./frontend/nginx.conf` still point to `:8000` and must be updated to `:8081` when connecting frontend to backend.

## 2. Tech Stack

### Backend (`./Veterinaria`)

* **Language & Runtime**: Java 25 (`eclipse-temurin:25`), Maven Wrapper (`./mvnw`, `./mvnw.cmd`).
* **Framework**: Spring Boot `4.1.1` (`spring-boot-starter-webmvc`), Lombok.
* **Database & ORM**: Spring Data JPA / Hibernate (`spring-boot-starter-data-jpa`), H2 in-memory (`jdbc:h2:mem:idat_proyecto_db`, console at `/h2-console`) for dev/test; PostgreSQL 15 target for production (`RNF-17`).
* **Security**: Spring Security (`spring-boot-starter-security`) with stateless `BCryptPasswordEncoder` + JDK `HmacSHA256` JWT filter (`OncePerRequestFilter`).
* **Testing**: JUnit 5, MockMvc, `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`, `spring-boot-starter-security-test`.

### Frontend (`./frontend`)

* **Language & Bundler**: JavaScript (ES Modules, `.jsx`), React `19.2`, Vite `8.1`, React Router `7.18`.
* **State & HTTP**: Zustand `5.0` (`src/store/authStore.js`), Axios `1.18` (`src/api/axiosConfig.js`), `jwt-decode`.
* **Styling & UI**: TailwindCSS `4.3` (`@tailwindcss/vite`), Lucide React, Framer Motion, GSAP, Three.js (`@react-three/fiber`, `@react-three/drei`), Lottie, Recharts, `jspdf` + `jspdf-autotable`, `html5-qrcode`.
* **Linting / Testing**: `oxlint` `1.71` (`npm run lint`). No TypeScript (`typecheck`) or frontend test runner is configured.

## 3. Commands

### Backend (`cd Veterinaria`)

* **Install / resolve dependencies**:
  ```powershell
  ./mvnw.cmd dependency:resolve
  ```
* **Run dev server** (port `8081`):
  ```powershell
  ./mvnw.cmd spring-boot:run
  ```
* **Build JAR** (skip tests):
  ```powershell
  ./mvnw.cmd clean package -DskipTests
  ```
* **Compile / typecheck**:
  ```powershell
  ./mvnw.cmd test-compile
  ```
* **Run all tests**:
  ```powershell
  ./mvnw.cmd test
  ```
* **Run a single test class or method**:
  ```powershell
  ./mvnw.cmd test "-Dtest=VeterinariaApplicationTests"
  ./mvnw.cmd test "-Dtest=VeterinariaApplicationTests#contextLoads"
  ```

### Frontend (`cd frontend`)

* **Install dependencies**:
  ```powershell
  npm install
  ```
* **Run dev server** (port `5173`):
  ```powershell
  npm run dev
  ```
* **Build production bundle**:
  ```powershell
  npm run build
  ```
* **Lint**:
  ```powershell
  npm run lint
  ```
* **Preview production build**:
  ```powershell
  npm run preview
  ```
