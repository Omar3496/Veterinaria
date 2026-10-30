package com.tecsolutions.Veterinaria;

import com.tecsolutions.Veterinaria.config.DataSeeder;
import com.tecsolutions.Veterinaria.model.*;
import com.tecsolutions.Veterinaria.repository.*;
import com.tecsolutions.Veterinaria.security.JwtUtil;
import com.tecsolutions.Veterinaria.security.RateLimitingFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ReportesAndDataSeederIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ExamenAuxiliarRepository examenAuxiliarRepository;

    @Autowired
    private OrdenExamenRepository ordenExamenRepository;

    @Autowired
    private RecetaMedicaRepository recetaMedicaRepository;

    @Autowired
    private ReferenciaMedicaRepository referenciaMedicaRepository;

    @Autowired
    private VacunaRepository vacunaRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private FamiliaRepository familiaRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private StockAlmacenRepository stockAlmacenRepository;

    @Autowired
    private MovimientoStockRepository movimientoStockRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private DetalleVentaRepository detalleVentaRepository;

    @Autowired
    private NotaCreditoRepository notaCreditoRepository;

    @Autowired
    private DevolucionRepository devolucionRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSeeder dataSeeder;

    private Usuario adminUser;
    private Usuario vetUser;
    private Usuario recepUser;
    private Usuario clienteUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        rateLimitingFilter.reset();
        limpiarBaseDatos();

        adminUser = new Usuario(null, "admin", passwordEncoder.encode("admin123"), RolEnum.ADMIN, true, "admin@sirbull.pe");
        adminUser = usuarioRepository.save(adminUser);

        vetUser = new Usuario(null, "vet", passwordEncoder.encode("vet123"), RolEnum.VETERINARIO, true, "vet@sirbull.pe");
        vetUser = usuarioRepository.save(vetUser);

        recepUser = new Usuario(null, "recep", passwordEncoder.encode("recep123"), RolEnum.RECEPCIONISTA, true, "recep@sirbull.pe");
        recepUser = usuarioRepository.save(recepUser);

        clienteUser = new Usuario(null, "cliente", passwordEncoder.encode("cliente123"), RolEnum.CLIENTE, true, "cliente@sirbull.pe");
        clienteUser = usuarioRepository.save(clienteUser);
    }

    @AfterEach
    void tearDown() {
        limpiarBaseDatos();
    }

    private void limpiarBaseDatos() {
        devolucionRepository.deleteAll();
        notaCreditoRepository.deleteAll();
        detalleVentaRepository.deleteAll();
        ventaRepository.deleteAll();
        movimientoStockRepository.deleteAll();
        stockAlmacenRepository.deleteAll();
        productoRepository.deleteAll();
        proveedorRepository.deleteAll();
        categoriaRepository.deleteAll();
        familiaRepository.deleteAll();
        marcaRepository.deleteAll();
        vacunaRepository.deleteAll();
        referenciaMedicaRepository.deleteAll();
        recetaMedicaRepository.deleteAll();
        ordenExamenRepository.deleteAll();
        consultaRepository.deleteAll();
        citaRepository.deleteAll();
        examenAuxiliarRepository.deleteAll();
        mascotaRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        auditoriaRepository.deleteAll();
        clienteRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private String tokenFor(Usuario usuario) {
        return jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().getValue());
    }

    private Mascota crearMascota(String nombre, String especie, String raza, LocalDate fechaNac, Cliente cliente) {
        Mascota m = new Mascota();
        m.setNombre(nombre);
        m.setEspecie(especie);
        m.setRaza(raza);
        m.setFechaNacimiento(fechaNac);
        m.setPropietario(cliente);
        return mascotaRepository.save(m);
    }

    private Producto crearProducto(String sku, String nombre, Double precio, Integer stock, Integer stockMinimo) {
        Producto p = new Producto();
        p.setSku(sku);
        p.setNombre(nombre);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setStockMinimo(stockMinimo);
        p.setPrecioCompra(precio != null ? Math.round(precio * 0.7 * 100.0) / 100.0 : 0.0);
        p.setEstadoAlmacen(EstadoAlmacen.DISPONIBLE);
        return productoRepository.save(p);
    }

    private Cita crearCita(LocalDateTime fechaHora, String motivo, EstadoCita estado, Mascota mascota, Usuario vet) {
        Cita c = new Cita();
        c.setFechaHora(fechaHora);
        c.setMotivo(motivo);
        c.setTipo("Consulta médica");
        c.setEstado(estado);
        c.setMascota(mascota);
        c.setVeterinario(vet);
        return citaRepository.save(c);
    }

    @Nested
    @DisplayName("1. Root Health Check & Public Endpoints")
    class RootHealthCheckTests {

        @Test
        @DisplayName("GET / is publicly accessible and returns 200 with online status message")
        void testRootEndpointPublic() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message", is("Sirbull Pet API - Online")));
        }
    }

    @Nested
    @DisplayName("2. DataSeeder Execution & Idempotence Tests")
    class DataSeederTests {

        @Test
        @DisplayName("DataSeeder seeds demo users, clients, pets, exams, provider, and 47 products")
        void testDataSeederExecution() {
            limpiarBaseDatos();
            assertEquals(0, usuarioRepository.count());

            dataSeeder.seed();

            // 5 Demo users
            assertEquals(5, usuarioRepository.count());
            assertTrue(usuarioRepository.findByUsername("admin").isPresent());
            assertTrue(usuarioRepository.findByUsername("vet").isPresent());
            assertTrue(usuarioRepository.findByUsername("recep").isPresent());
            assertTrue(usuarioRepository.findByUsername("jperez").isPresent());
            assertTrue(usuarioRepository.findByUsername("mlopez").isPresent());

            // 2 Demo clients
            assertEquals(2, clienteRepository.count());
            assertTrue(clienteRepository.findByDni("12345678").isPresent());
            assertTrue(clienteRepository.findByDni("87654321").isPresent());

            // 3 Demo pets
            assertEquals(3, mascotaRepository.count());

            // 8 Demo ExamenAuxiliar items
            assertEquals(8, examenAuxiliarRepository.count());

            // 1 Provider
            assertEquals(1, proveedorRepository.count());

            // 47 Products (P001, P002, P003..P047)
            assertEquals(47, productoRepository.count());
            assertTrue(productoRepository.findBySku("P001").isPresent());
            assertTrue(productoRepository.findBySku("P002").isPresent());
            assertTrue(productoRepository.findBySku("P047").isPresent());

            // 47 StockAlmacen ("Disponible")
            assertEquals(47, stockAlmacenRepository.count());

            // 2 Sample appointments
            assertEquals(2, citaRepository.count());
        }

        @Test
        @DisplayName("DataSeeder is idempotent and skips seeding when users already exist")
        void testDataSeederIdempotence() {
            limpiarBaseDatos();
            dataSeeder.seed();

            long userCountFirst = usuarioRepository.count();
            long prodCountFirst = productoRepository.count();
            long clientCountFirst = clienteRepository.count();
            long examCountFirst = examenAuxiliarRepository.count();

            assertEquals(5, userCountFirst);
            assertEquals(47, prodCountFirst);
            assertEquals(2, clientCountFirst);
            assertEquals(8, examCountFirst);

            // Run seed again
            dataSeeder.seed();

            // Counts must remain identical
            assertEquals(userCountFirst, usuarioRepository.count());
            assertEquals(prodCountFirst, productoRepository.count());
            assertEquals(clientCountFirst, clienteRepository.count());
            assertEquals(examCountFirst, examenAuxiliarRepository.count());
        }
    }

    @Nested
    @DisplayName("3. Dashboard KPIs & Próximas Citas Tests")
    class DashboardStatsAndProximasCitasTests {

        @Test
        @DisplayName("GET /api/stats RBAC: 200 for Admin & Vet, 403 for Recep & Cliente, 401 unauthenticated")
        void testStatsRbac() throws Exception {
            mockMvc.perform(get("/api/stats"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/stats")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/stats")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/stats")
                            .header("Authorization", "Bearer " + tokenFor(vetUser)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/stats")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/stats returns accurate metrics for citas_hoy, vacunas_pendientes, alertas_stock")
        void testStatsMetricsAccuracy() throws Exception {
            Cliente cli = new Cliente();
            cli.setDni("11223344");
            cli.setNombre("Carlos Test");
            cli.setTelefono("987654321");
            cli.setEmail("carlos@test.pe");
            cli = clienteRepository.save(cli);

            Mascota pet = crearMascota("Bobi", "Perro", "Chihuahua", LocalDate.now().minusYears(2), cli);

            // Cita hoy (Programada)
            crearCita(LocalDateTime.now().withHour(10).withMinute(0), "Consulta general", EstadoCita.PROGRAMADA, pet, vetUser);

            // Cita hoy (Completada)
            crearCita(LocalDateTime.now().withHour(14).withMinute(30), "Chequeo dental", EstadoCita.COMPLETADA, pet, vetUser);

            // Cita manana (Programada)
            crearCita(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0), "Vacuna puppy", EstadoCita.PROGRAMADA, pet, vetUser);

            // Cita ayer (Programada - expired)
            crearCita(LocalDateTime.now().minusDays(1).withHour(11).withMinute(0), "Control pasado", EstadoCita.PROGRAMADA, pet, vetUser);

            // Low stock products: stock <= stock_minimo
            crearProducto("TEST001", "Prod Stock Bajo 1", 20.0, 3, 5); // low
            crearProducto("TEST002", "Prod Stock Limite", 30.0, 5, 5); // low (<=)
            crearProducto("TEST003", "Prod Stock Normal", 40.0, 15, 5); // ok

            mockMvc.perform(get("/api/stats")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.citas_hoy", is(2)))
                    .andExpect(jsonPath("$.vacunas_pendientes", is(2))) // c1 (hoy) + c3 (manana)
                    .andExpect(jsonPath("$.alertas_stock", is(2))); // p1, p2
        }

        @Test
        @DisplayName("GET /api/citas/proximas RBAC: 200 for Admin, Vet & Recep, 403 for Cliente")
        void testProximasCitasRbac() throws Exception {
            mockMvc.perform(get("/api/citas/proximas"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/citas/proximas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/citas/proximas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/citas/proximas")
                            .header("Authorization", "Bearer " + tokenFor(vetUser)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/citas/proximas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/citas/proximas returns formatted list with id, hora, mascota, propietario, motivo, estado, veterinario")
        void testProximasCitasFormat() throws Exception {
            Cliente cli = new Cliente();
            cli.setDni("99887766");
            cli.setNombre("Elena Ramos");
            cli.setTelefono("912345678");
            cli.setEmail("elena@test.pe");
            cli = clienteRepository.save(cli);

            Mascota pet = crearMascota("Pelusa", "Gato", "Persa", null, cli);

            crearCita(LocalDateTime.of(2026, 11, 15, 10, 0), "Vacunación Anual", EstadoCita.PROGRAMADA, pet, vetUser);

            mockMvc.perform(get("/api/citas/proximas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].hora", is("10:00 AM")))
                    .andExpect(jsonPath("$[0].mascota", is("Pelusa (Gato)")))
                    .andExpect(jsonPath("$[0].propietario", is("Elena Ramos")))
                    .andExpect(jsonPath("$[0].motivo", is("Vacunación Anual")))
                    .andExpect(jsonPath("$[0].estado", is("Programada")))
                    .andExpect(jsonPath("$[0].veterinario", is("vet")));
        }
    }

    @Nested
    @DisplayName("4. Analytical Reports: Ventas & Top Productos Tests")
    class AnalyticalReportsVentasAndProductosTests {

        @Test
        @DisplayName("GET /api/reportes/ventas RBAC: 200 for Admin & Recep, 403 for Vet & Cliente")
        void testReporteVentasRbac() throws Exception {
            mockMvc.perform(get("/api/reportes/ventas"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/reportes/ventas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/reportes/ventas")
                            .header("Authorization", "Bearer " + tokenFor(vetUser)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/reportes/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/reportes/ventas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/reportes/ventas aggregates daily sales and respects fecha_inicio and fecha_fin filters")
        void testReporteVentasDataAndFilters() throws Exception {
            Cliente cli = new Cliente();
            cli.setDni("55443322");
            cli.setNombre("Pedro Sales");
            cli.setTelefono("987111222");
            cli.setEmail("pedro@test.pe");
            cli = clienteRepository.save(cli);

            // Day 1: 2026-10-10 -> two sales (total 100 + 50 = 150)
            Venta v1 = new Venta();
            v1.setCliente(cli);
            v1.setUsuario(adminUser);
            v1.setTotal(100.0);
            v1.setFecha(LocalDateTime.of(2026, 10, 10, 11, 0));
            ventaRepository.save(v1);

            Venta v2 = new Venta();
            v2.setCliente(cli);
            v2.setUsuario(adminUser);
            v2.setTotal(50.0);
            v2.setFecha(LocalDateTime.of(2026, 10, 10, 16, 30));
            ventaRepository.save(v2);

            // Day 2: 2026-10-15 -> one sale (total 200)
            Venta v3 = new Venta();
            v3.setCliente(cli);
            v3.setUsuario(adminUser);
            v3.setTotal(200.0);
            v3.setFecha(LocalDateTime.of(2026, 10, 15, 9, 15));
            ventaRepository.save(v3);

            // Day 3: 2026-10-25 -> one sale (total 80)
            Venta v4 = new Venta();
            v4.setCliente(cli);
            v4.setUsuario(adminUser);
            v4.setTotal(80.0);
            v4.setFecha(LocalDateTime.of(2026, 10, 25, 14, 0));
            ventaRepository.save(v4);

            // Unfiltered: all 3 days
            mockMvc.perform(get("/api/reportes/ventas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].fecha", is("2026-10-10")))
                    .andExpect(jsonPath("$[0].total", is(150.0)))
                    .andExpect(jsonPath("$[1].fecha", is("2026-10-15")))
                    .andExpect(jsonPath("$[1].total", is(200.0)))
                    .andExpect(jsonPath("$[2].fecha", is("2026-10-25")))
                    .andExpect(jsonPath("$[2].total", is(80.0)));

            // Filtered: from 2026-10-12 to 2026-10-20 (only Day 2 matches)
            mockMvc.perform(get("/api/reportes/ventas")
                            .param("fecha_inicio", "2026-10-12")
                            .param("fecha_fin", "2026-10-20")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].fecha", is("2026-10-15")))
                    .andExpect(jsonPath("$[0].total", is(200.0)));
        }

        @Test
        @DisplayName("GET /api/reportes/productos returns top 10 products sorted by quantity sold descending")
        void testReporteProductosTop10() throws Exception {
            Cliente cli = new Cliente();
            cli.setDni("77665544");
            cli.setNombre("Ana Top");
            cli.setTelefono("987222333");
            cli.setEmail("ana@test.pe");
            cli = clienteRepository.save(cli);

            Producto p1 = crearProducto("SKU1", "Dog Food Premium", 50.0, 100, 5);
            Producto p2 = crearProducto("SKU2", "Cat Food Tuna", 40.0, 100, 5);
            Producto p3 = crearProducto("SKU3", "Shampoo Antipulgas", 25.0, 100, 5);

            Venta venta = new Venta();
            venta.setCliente(cli);
            venta.setUsuario(adminUser);
            venta.setTotal(300.0);
            venta.setFecha(LocalDateTime.of(2026, 10, 18, 12, 0));
            venta = ventaRepository.save(venta);

            // Detalle 1: p1 -> 15 units
            detalleVentaRepository.save(new DetalleVenta(null, venta, p1, 15, 50.0, 750.0));
            // Detalle 2: p2 -> 30 units (top 1)
            detalleVentaRepository.save(new DetalleVenta(null, venta, p2, 30, 40.0, 1200.0));
            // Detalle 3: p3 -> 5 units
            detalleVentaRepository.save(new DetalleVenta(null, venta, p3, 5, 25.0, 125.0));

            mockMvc.perform(get("/api/reportes/productos")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].nombre", is("Cat Food Tuna")))
                    .andExpect(jsonPath("$[0].cantidad", is(30)))
                    .andExpect(jsonPath("$[1].nombre", is("Dog Food Premium")))
                    .andExpect(jsonPath("$[1].cantidad", is(15)))
                    .andExpect(jsonPath("$[2].nombre", is("Shampoo Antipulgas")))
                    .andExpect(jsonPath("$[2].cantidad", is(5)));
        }
    }

    @Nested
    @DisplayName("5. Analytical Reports: Citas Mensuales & Demografía Tests")
    class AnalyticalReportsCitasAndDemografiaTests {

        @Test
        @DisplayName("GET /api/reportes/citas RBAC: 200 for Admin & Recep, 403 for Vet & Cliente")
        void testReporteCitasRbac() throws Exception {
            mockMvc.perform(get("/api/reportes/citas"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/reportes/citas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/reportes/citas")
                            .header("Authorization", "Bearer " + tokenFor(vetUser)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(get("/api/reportes/citas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/reportes/citas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/reportes/citas aggregates appointments by month and status")
        void testReporteCitasMonthlyAggregation() throws Exception {
            Cliente cli = new Cliente();
            cli.setDni("66554433");
            cli.setNombre("Sofia Citas");
            cli.setTelefono("999333444");
            cli.setEmail("sofia@test.pe");
            cli = clienteRepository.save(cli);

            Mascota pet = crearMascota("Luna", "Gato", "Angora", null, cli);

            // October 2026: 2 Completada, 1 Cancelada, 1 Programada
            crearCita(LocalDateTime.of(2026, 10, 5, 10, 0), "Motivo", EstadoCita.COMPLETADA, pet, vetUser);
            crearCita(LocalDateTime.of(2026, 10, 12, 11, 0), "Motivo", EstadoCita.COMPLETADA, pet, vetUser);
            crearCita(LocalDateTime.of(2026, 10, 18, 15, 0), "Motivo", EstadoCita.CANCELADA, pet, vetUser);
            crearCita(LocalDateTime.of(2026, 10, 28, 9, 0), "Motivo", EstadoCita.PROGRAMADA, pet, vetUser);

            // November 2026: 2 Programada
            crearCita(LocalDateTime.of(2026, 11, 2, 10, 0), "Motivo", EstadoCita.PROGRAMADA, pet, vetUser);
            crearCita(LocalDateTime.of(2026, 11, 15, 16, 0), "Motivo", EstadoCita.PROGRAMADA, pet, vetUser);

            mockMvc.perform(get("/api/reportes/citas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].mes", is("2026-10")))
                    .andExpect(jsonPath("$[0].Completada", is(2)))
                    .andExpect(jsonPath("$[0].Cancelada", is(1)))
                    .andExpect(jsonPath("$[0].Programada", is(1)))
                    .andExpect(jsonPath("$[0].cantidad", is(4)))
                    .andExpect(jsonPath("$[1].mes", is("2026-11")))
                    .andExpect(jsonPath("$[1].Completada", is(0)))
                    .andExpect(jsonPath("$[1].Cancelada", is(0)))
                    .andExpect(jsonPath("$[1].Programada", is(2)))
                    .andExpect(jsonPath("$[1].cantidad", is(2)));
        }

        @Test
        @DisplayName("GET /api/reportes/demografia groups pets by species capitalized")
        void testReporteDemografiaGrouping() throws Exception {
            Cliente cli = new Cliente();
            cli.setDni("44332211");
            cli.setNombre("Lucas Demo");
            cli.setTelefono("999555666");
            cli.setEmail("lucas@test.pe");
            cli = clienteRepository.save(cli);

            // Species with various casings
            crearMascota("Pet 1", "perro", "Mestizo", null, cli);
            crearMascota("Pet 2", "Perro", "Bulldog", null, cli);
            crearMascota("Pet 3", "PERRO", "Golden", null, cli);
            crearMascota("Pet 4", "gato", "Comun", null, cli);
            crearMascota("Pet 5", "GATO", "Siames", null, cli);
            crearMascota("Pet 6", "conejo", "Enano", null, cli);

            mockMvc.perform(get("/api/reportes/demografia")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[?(@.name == 'Perro')].value", contains(3)))
                    .andExpect(jsonPath("$[?(@.name == 'Gato')].value", contains(2)))
                    .andExpect(jsonPath("$[?(@.name == 'Conejo')].value", contains(1)))
                    // Frontend compatibility aliases:
                    .andExpect(jsonPath("$[?(@.especie == 'Perro')].cantidad", contains(3)));
        }
    }
}
