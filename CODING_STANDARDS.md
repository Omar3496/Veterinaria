# Coding Standards & Architecture Guidelines (`Veterinaria`)

This document defines the core standards and architectural rules for the Spring Boot backend (`Veterinaria`). All PRs, code reviews, and agent implementations are bound by these rules.

---

## 1. Concurrency & In-Memory Stores (Ponytail Rule 42)

Every in-memory collection, cache, or sliding window with known clustering or scaling limits (e.g., `ConcurrentHashMap`, `synchronized` blocks, unbounded memory buffers) **must** declare its boundary explicitly:

```java
// shortcut: <known limit>, <when to upgrade>
private final Map<String, List<Long>> requestLog = new ConcurrentHashMap<>();
```

- **Rule**: Do not add distributed tools (Redis, Kafka) prematurely, but never hide in-memory shortcuts without declaring their upgrade trigger.
- **Review Check**: Mechanical inspection rejects unannotated concurrency stores.

---

## 2. JPA & Query Integrity

### No Implicit Joins Across Nullable Associations
- **Forbidden**: Traversing nullable `@ManyToOne` or `@OneToOne` associations implicitly in JPQL `@Query` (e.g., `WHERE o.cita.mascota.id = :id`). Hibernate generates an implicit `INNER JOIN` that silently filters out rows where `o.cita` is `null`.
- **Mandatory**: Use explicit `LEFT JOIN` syntax or separate repository queries combined in the service layer:
  ```java
  // Good: Explicit left joins
  @Query("SELECT o FROM OrdenExamen o LEFT JOIN o.cita c LEFT JOIN o.consulta con WHERE ...")
  // Or: Separate repository queries combined in service
  List<OrdenExamen> list1 = repo.findByConsultaMascotaId(...);
  List<OrdenExamen> list2 = repo.findByCitaMascotaIdIn(...);
  ```

---

## 3. State Machine & Financial Mutation Idempotency

Every financial, inventory, or order reversal mutation (`cancelarCita`, `cancelarVenta`, `anularIngreso`, `devolverVenta`) **must** guard against repeated invocation:

- **Forbidden**: Blindly executing stock increment/decrement or issuing store credit without checking current state.
- **Mandatory**:
  - Check whether the entity is already in terminal/cancelled state (`if (venta.getEstado() == VentaEstado.CANCELADA) ...`).
  - Check whether a return or refund record already exists (`if (devolucionRepository.existsByVentaId(id)) ...`).
  - Reject duplicate requests with `400 Bad Request` or `404 Not Found` and a clear `{ "detail": "..." }` message.
  - Every mutation endpoint must include an automated integration test verifying that a second attempt fails safely.

---

## 4. Wire Contract & Security Parity (ADR-0001)

1. **Jackson Snake Case**:
   - Java code uses idiomatic `camelCase`.
   - `spring.jackson.property-naming-strategy=SNAKE_CASE` formats JSON keys in `snake_case` matching the React frontend.
2. **Spanish-Backed Enums**:
   - Dual parsing: `@JsonCreator` accepts both English constant name (`ADMIN`) and Spanish wire label (`Administrador`), case-insensitively.
   - Serialization: `@JsonValue` always outputs Spanish wire label (`Administrador`).
3. **Plain Java Mappers**:
   - All DTO conversions live as plain `@Component` classes in `com.tecsolutions.Veterinaria.mapper`.
   - Never add MapStruct or reflection-based mappers. DTO mapping must explicitly strip sensitive fields (e.g. `hashed_password`).
4. **Security Utilities**:
   - Use `com.tecsolutions.Veterinaria.security.SecurityUtils` for extracting principal attributes (`extractUsername`, `extractRole`, `isClienteRole`, `isStaffRole`, `isAdminRole`).
   - Do not copy-paste authentication extraction logic across controllers.
5. **Unified Error Contract**:
   - All errors must return `{ "detail": "<message>" }` handled by `GlobalExceptionHandler`.
