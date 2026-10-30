# 07: Dashboard KPIs, Analytical Reportes & Idempotent DataSeeder

**What to build:** End-to-end root health check, real-time operational dashboard KPIs (`GET /api/stats` and `GET /api/citas/proximas`), date-filtered analytical reports (`GET /api/reportes/ventas`, `GET /api/reportes/productos`, `GET /api/reportes/citas`, `GET /api/reportes/demografia`), and the idempotent startup `DataSeeder` that populates the demo `Usuario` accounts (`admin`, `vet`, `recep`, `jperez`, `mlopez`), `Cliente` and `Mascota` records, sample `Cita` entries, the 8 `ExamenAuxiliar` catalog items, the demo `Proveedor`, `Categoria`/`Familia`/`Marca` classifications, and all 47 store `Producto` items with `StockAlmacen`.

**Blocked by:** 03: Configuracion, ExamenAuxiliar Catalog, Cita Scheduling & Pago Billing, 04: Clinical Encounters: Consulta, RecetaMedica, OrdenExamen, ReferenciaMedica, Vacuna & Reminders, 05: Inventory Catalogs, Producto, Multi-Warehouse StockAlmacen, MovimientoStock & Ingreso, 06: POS & Online Venta, ComprobanteSeq Billing, Cancellation, Devolucion & NotaCredito

**Status:** resolved

- [x] `GET /` is publicly accessible and returns `{ "message": "Sirbull Pet API - Online" }`.
- [x] `GET /api/stats` (for `Administrador` and `Veterinario`) returns `{ "citas_hoy", "vacunas_pendientes", "alertas_stock" }`, and `GET /api/citas/proximas` (for `Administrador`, `Veterinario`, and `Recepcionista`) returns the formatted list of appointments (`id`, `hora`, `mascota`, `propietario`, `motivo`, `estado`, `veterinario`).
- [x] `GET /api/reportes/ventas` and `GET /api/reportes/productos` (for `Administrador` and `Recepcionista`) support optional `fecha_inicio` and `fecha_fin` filters and return daily sales totals (`[{ "fecha", "total" }]`) and top 10 products by units sold (`[{ "nombre", "cantidad" }]`).
- [x] `GET /api/reportes/citas` (with optional `fecha_inicio` and `fecha_fin`) returns monthly appointment counts grouped by status (`[{ "mes", "Completada", "Cancelada", "Programada" }]`), and `GET /api/reportes/demografia` returns pet counts grouped by capitalized species (`[{ "name", "value" }]`).
- [x] Idempotent `DataSeeder` (`CommandLineRunner`) seeds the demo users (`admin`/`admin123`, `vet`/`vet123`, `recep`/`recep123`, `jperez`/`cliente123`, `mlopez`/`cliente123`), demo `Cliente` and `Mascota` records, sample `Cita` records, the 8 `ExamenAuxiliar` catalog items, the demo `Proveedor`, `Categoria`/`Familia`/`Marca` records, and the store `Producto` catalog with numeric `2026XXXX` SKUs and `StockAlmacen("Disponible")` records without duplicating existing data on restart.
- [x] End-to-end `MockMvc` tests verify the health check, dashboard KPIs, all 4 analytical report endpoints, and idempotent seeding.
