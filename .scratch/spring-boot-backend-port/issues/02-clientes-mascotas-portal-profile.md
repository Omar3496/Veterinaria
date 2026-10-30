# 02: Cliente & Mascota Management and Portal Profile

**What to build:** End-to-end management of `Cliente` and `Mascota` records for clinic staff and the customer self-service portal. Staff (`Administrador`, `Veterinario`, `Recepcionista`) can list and register `Cliente` records (`GET/POST /api/clientes`) with unique `dni` and `telefono` validation and optional portal account creation (`username` and `password`), while only `Administrador` can delete a `Cliente` (`DELETE /api/clientes/{id}`), cascading deletion to their `Mascota` and `Cita` records. Logged-in `Cliente` users can fetch their own profile (`GET /api/clientes/me`, including nested `mascotas`) and update their profile photo (`PUT /api/clientes/me/foto`). Staff can register a `Mascota` under a `Cliente` (`POST /api/clientes/{id}/mascotas`), and both staff and `Cliente` users can list pets (`GET /api/mascotas`, filtered to the caller's own pets for `Cliente`) and update pet photos (`PUT /api/mascotas/{id}/foto`, enforcing ownership for `Cliente`).

**Blocked by:** 01: Security, JWT Authentication, Usuario CRUD, Password Reset & Auditoria

**Status:** resolved

- [x] `GET /api/clientes` (with `skip` and `limit` pagination) and `POST /api/clientes` allow `Administrador`, `Veterinario`, and `Recepcionista` to list and create `Cliente` records with nested `mascotas`, optionally creating a linked `Usuario` with role `Cliente` when both `username` and `password` are provided (returning `HTTP 400` if only one is provided or if `username`, `dni`, or `telefono` is duplicated).
- [x] `DELETE /api/clientes/{id}` is restricted to `Administrador` and cascades deletion to associated `Mascota` records.
- [x] `GET /api/clientes/me` and `PUT /api/clientes/me/foto` allow an authenticated `Cliente` to retrieve their own profile and update `foto_url` from `foto_base64`.
- [x] `POST /api/clientes/{id}/mascotas` allows `Administrador`, `Veterinario`, and `Recepcionista` to create a `Mascota` linked to the specified `Cliente` (`HTTP 404` if `Cliente` does not exist).
- [x] `GET /api/mascotas` returns all pets for staff (`Administrador`, `Veterinario`, `Recepcionista`) and only the authenticated owner's pets for `Cliente`.
- [x] `PUT /api/mascotas/{id}/foto` updates `foto_url` from `foto_base64`, enforcing that a `Cliente` can only modify their own `Mascota` (`HTTP 403` otherwise).
- [x] End-to-end `MockMvc` tests verify `Cliente` and `Mascota` CRUD, optional portal credential creation, photo updates, and `Cliente` ownership isolation.
