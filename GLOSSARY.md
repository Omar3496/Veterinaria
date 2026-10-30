# Sirbull Pet Clinic

Integrated veterinary clinic management, multi-warehouse inventory, POS billing, and customer self-service portal.

## Language

### Identity & Access

**Usuario**:
A system account with credentials (`username`, `hashed_password`) and a single role (`RolEnum`: `Administrador`, `Veterinario`, `Recepcionista`, `Cliente`) governing RBAC permissions.
_Avoid_: Account, Staff, Veterinario (as a separate entity)

**Cliente**:
A pet owner and store buyer identified by a unique fiscal/identity document (`dni`) and phone (`telefono`), optionally linked to a `Usuario` with role `Cliente` for portal access.
_Avoid_: Propietario, Owner, Buyer

**Mascota**:
An animal patient belonging to a single `Cliente`, with species, breed, birth date, active status, clinical history, appointments, and vaccinations.
_Avoid_: Paciente, Pet

### Clinical & Scheduling

**Cita**:
A scheduled time slot for a `Mascota` (optionally assigned to a `Usuario` with role `Veterinario`) for a specific service type (`Consulta médica`, `Control / Seguimiento`, `Vacunación`, `Baño / Aseo`, `Examen auxiliar`, `Cirugía`), progressing through `Pendiente`, `Programada`, `Completada`, or `Cancelada`.
_Avoid_: Turno, Reserva, Appointment

**Consulta**:
A clinical encounter record created by a veterinarian directly for a `Mascota` or by attending a scheduled `Cita`, capturing weight, diagnosis, treatment, and issuing linked `RecetaMedica`, `OrdenExamen`, and `ReferenciaMedica` records.
_Avoid_: Atención, Visita, Historial

**ExamenAuxiliar**:
A master catalog entry for a diagnostic or laboratory test offered by the clinic, including price, estimated turnaround time, and active status.
_Avoid_: Análisis, Laboratorio, Test

**OrdenExamen**:
A clinical order requesting a specific `ExamenAuxiliar` for a `Mascota` (created either during a `Consulta` or automatically when booking an `Examen auxiliar` `Cita`), tracking status (`PENDIENTE`, `REALIZADO`, `ANULADA`) and results.
_Avoid_: Solicitud de examen, Resultado de examen

**RecetaMedica**:
A prescription item issued during a `Consulta`, specifying a medication name (optionally linked to an inventory `Producto`), dosage, quantity, and instructions.
_Avoid_: Prescripción, Orden de medicamento

**ReferenciaMedica**:
A clinical referral issued during a `Consulta` directing a `Mascota` to an external facility and specialty for specialized care.
_Avoid_: Derivación, Interconsulta

**Vacuna**:
A vaccination record applied to a `Mascota` on a specific date, optionally deducting one unit from `Disponible` stock of a linked `Producto` and computing `proxima_dosis` from `intervalo_dias` for 15-day reminder alerts.
_Avoid_: Inmunización, Dosis

### Inventory & Purchasing

**Producto**:
A retail or clinical item identified by a correlative `sku` (`2026XXXX`), priced for purchase and sale, classified by `Proveedor`, `Categoria`, `Familia`, and `Marca`, and tracked for perishability and minimum stock alerts.
_Avoid_: Artículo, Ítem, Mercadería

**StockAlmacen**:
The quantity breakdown of a `Producto` across warehouse states (`Disponible`, `No disponible`, `Dañado`, `Vencido`).
_Avoid_: Inventario por estado, Existencia

**MovimientoStock**:
An immutable kardex ledger entry recording every stock quantity or warehouse-state change (`INGRESO`, `ANULACION_INGRESO`, `VENTA`, `AJUSTE`, `CAMBIO_ALMACEN`, `DEVOLUCION`, `ANULACION_VENTA`).
_Avoid_: Kardex, Historial de inventario, Transacción de stock

**Ingreso**:
A merchandise purchase receipt from a `Proveedor` containing `IngresoDetalle` line items that increment `Disponible` stock and update purchase/sale prices.
_Avoid_: Compra, Orden de compra, Recepción

### POS, Billing & Auditing

**Venta**:
A POS or online store purchase by a `Cliente` containing `DetalleVenta` line items, a pickup code (`SRB-XXXXXX`), a correlative electronic receipt (`Boleta` `BA64` or `Factura` `FA64`), and lifecycle state (`Pendiente`, `Confirmada`, `Entregada`, `Anulación solicitada`, `Cancelada`).
_Avoid_: Pedido, Orden, Compra de cliente

**Pago**:
A payment transaction for a `Cita` (`YAPE`, `PLIN`, `TARJETA`, `EFECTIVO`) that transitions a `Pendiente` appointment to `Programada` and issues a correlative `Boleta` (`BA64`) or `Factura` (`FA64`).
_Avoid_: Cobro, Transacción

**Devolucion**:
A post-delivery return processed against an `Entregada` `Venta` that restores returned units to `Disponible` stock and settles via cash reversal (`Extorno`) or a `NotaCredito`.
_Avoid_: Reembolso, Anulación

**NotaCredito**:
A store credit voucher (`NC-XXXXXX`, valid for 180 days with remaining `saldo`) issued from a `Devolucion` and redeemable toward future `Venta` transactions.
_Avoid_: Cupón, Vale, Saldo a favor

**Auditoria**:
An automated security and traceability log entry recording every successful mutating API request (`POST`, `PUT`, `DELETE` under `/api/*`) and `LOGIN` event.
_Avoid_: Bitácora, Historial de acciones
