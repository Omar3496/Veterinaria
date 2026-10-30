# Ticket 03: End-to-End Smoke Verification & Live API Testing

Status: resolved
Blocked by: 02

## Objectives
1. Verify backend execution on `:8081` with H2 database. (Done)
2. Execute smoke tests verifying:
   - `POST /token` for admin (`admin`/`admin123`) returning JWT with `rol: "Administrador"`. (Done)
   - `POST /token` for client (`jperez`/`cliente123`) returning JWT with `rol: "Cliente"`. (Done)
   - Data retrieval through API proxy: `/api/stats`, `/api/citas`, `/api/clientes`, `/api/mascotas`, `/api/clientes/me`. (Done)
   - Error response format validation returning `{ "detail": "..." }`. (Done)

## Acceptance Criteria
- Both staff and client authentication succeed. (Done)
- Proxy routes correctly without CORS issues. (Done)
- Smoke verification passes completely. (Done)

## Answer
- Live Spring Boot server started on port 8081 with H2 in-memory DB and DataSeeder.
- Vite dev server started on port 5173 with proxy configured for `/api` and `/token` forwarding to `http://localhost:8081`.
- Public query to `http://localhost:5173/api/productos` successfully returned 47 seeded products.
- Staff login (`admin`) succeeded, returned JWT with `rol: "Administrador"`, authorized calls to `/api/stats` and `/api/citas` succeeded.
- Client login (`jperez`) succeeded, returned JWT with `rol: "Cliente"`, authorized calls to `/api/clientes/me` returned Juan Perez and `/api/mascotas` returned his 2 pets.
- Error wire contract verified on invalid credentials, returning HTTP 401 with `{"detail":"Incorrect username or password"}`.
