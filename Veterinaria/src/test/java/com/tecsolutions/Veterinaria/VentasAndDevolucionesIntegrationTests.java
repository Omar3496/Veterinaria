package com.tecsolutions.Veterinaria;

import com.tecsolutions.Veterinaria.dto.*;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class VentasAndDevolucionesIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

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
    private ComprobanteSeqRepository comprobanteSeqRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario adminUser;
    private Usuario recepUser;
    private Usuario vetUser;
    private Usuario clienteUser;
    private Cliente clientePrincipal;

    private Usuario otroClienteUser;
    private Cliente otroCliente;

    private Producto prodOk;
    private Producto prodPocoStock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        rateLimitingFilter.reset();

        cleanDatabase();

        adminUser = usuarioRepository.save(new Usuario(null, "admin", passwordEncoder.encode("admin123"), RolEnum.ADMIN, true, "admin@sirbull.pe"));
        recepUser = usuarioRepository.save(new Usuario(null, "recep", passwordEncoder.encode("recep123"), RolEnum.RECEPCIONISTA, true, "recep@sirbull.pe"));
        vetUser = usuarioRepository.save(new Usuario(null, "vet", passwordEncoder.encode("vet123"), RolEnum.VETERINARIO, true, "vet@sirbull.pe"));
        clienteUser = usuarioRepository.save(new Usuario(null, "cliente_tienda", passwordEncoder.encode("cliente123"), RolEnum.CLIENTE, true, "cliente@sirbull.pe"));
        otroClienteUser = usuarioRepository.save(new Usuario(null, "otro_cliente", passwordEncoder.encode("otro123"), RolEnum.CLIENTE, true, "otro@sirbull.pe"));

        clientePrincipal = new Cliente();
        clientePrincipal.setDni("11122233");
        clientePrincipal.setNombre("Cliente Principal");
        clientePrincipal.setTelefono("555111222");
        clientePrincipal.setUsuario(clienteUser);
        clientePrincipal = clienteRepository.save(clientePrincipal);

        otroCliente = new Cliente();
        otroCliente.setDni("88877766");
        otroCliente.setNombre("Otro Cliente");
        otroCliente.setTelefono("555888999");
        otroCliente.setUsuario(otroClienteUser);
        otroCliente = clienteRepository.save(otroCliente);

        prodOk = new Producto();
        prodOk.setSku("PROD-OK-01");
        prodOk.setNombre("Alimento Premium Perro");
        prodOk.setPrecio(50.0);
        prodOk.setStock(20);
        prodOk.setEstadoAlmacen(EstadoAlmacen.DISPONIBLE);
        prodOk = productoRepository.save(prodOk);
        stockAlmacenRepository.save(new StockAlmacen(null, prodOk, "Disponible", 20));

        prodPocoStock = new Producto();
        prodPocoStock.setSku("PROD-POCO-01");
        prodPocoStock.setNombre("Shampoo Medicado");
        prodPocoStock.setPrecio(25.0);
        prodPocoStock.setStock(2);
        prodPocoStock.setEstadoAlmacen(EstadoAlmacen.DISPONIBLE);
        prodPocoStock = productoRepository.save(prodPocoStock);
        stockAlmacenRepository.save(new StockAlmacen(null, prodPocoStock, "Disponible", 2));
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        devolucionRepository.deleteAll();
        detalleVentaRepository.deleteAll();
        ventaRepository.deleteAll();
        notaCreditoRepository.deleteAll();
        comprobanteSeqRepository.deleteAll();
        movimientoStockRepository.deleteAll();
        stockAlmacenRepository.deleteAll();
        productoRepository.deleteAll();
        auditoriaRepository.deleteAll();
        clienteRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private String tokenFor(Usuario usuario) {
        return jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().getValue());
    }

    @Nested
    @DisplayName("1. Venta Creation, Fiscal Document Validation & Numbering Tests")
    class VentaCreationTests {

        @Test
        @DisplayName("Should create Boleta sale with valid 8-digit DNI and generate BA64 correlative")
        void testCreateBoletaConDniValido() throws Exception {
            VentaCreateDTO createDTO = VentaCreateDTO.builder()
                    .clienteId(clientePrincipal.getId())
                    .metodoPago("Efectivo")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .detalles(List.of(
                            DetalleVentaCreateDTO.builder()
                                    .productoId(prodOk.getId())
                                    .cantidad(3)
                                    .precioUnitario(50.0)
                                    .build()
                    ))
                    .build();

            MvcResult result = mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.total").value(150.0))
                    .andExpect(jsonPath("$.estado").value("Pendiente"))
                    .andExpect(jsonPath("$.serie").value("BA64"))
                    .andExpect(jsonPath("$.numero").value("0000000001"))
                    .andExpect(jsonPath("$.comprobante").value("BA64-0000000001"))
                    .andExpect(jsonPath("$.codigo_recojo", startsWith("SRB-")))
                    .andExpect(jsonPath("$.detalles", hasSize(1)))
                    .andExpect(jsonPath("$.detalles[0].subtotal").value(150.0))
                    .andExpect(jsonPath("$.detalles[0].producto_nombre").value("Alimento Premium Perro"))
                    .andReturn();

            // Verify stock reduction in DB
            Producto p = productoRepository.findById(prodOk.getId()).orElseThrow();
            assertEquals(17, p.getStock());
            StockAlmacen sa = stockAlmacenRepository.findByProductoIdAndAlmacen(prodOk.getId(), "Disponible").orElseThrow();
            assertEquals(17, sa.getCantidad());

            // Verify MovimientoStock logged with -3
            List<MovimientoStock> movs = movimientoStockRepository.findAll();
            assertEquals(1, movs.size());
            assertEquals(-3, movs.get(0).getCantidad());
            assertEquals("VENTA", movs.get(0).getTipoMovimiento());

            // Create second sale to verify correlative increment
            VentaCreateDTO createDTO2 = VentaCreateDTO.builder()
                    .clienteId(clientePrincipal.getId())
                    .metodoPago("Tarjeta")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("87654321")
                    .detalles(List.of(
                            DetalleVentaCreateDTO.builder()
                                    .productoId(prodOk.getId())
                                    .cantidad(1)
                                    .precioUnitario(50.0)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.serie").value("BA64"))
                    .andExpect(jsonPath("$.numero").value("0000000002"))
                    .andExpect(jsonPath("$.comprobante").value("BA64-0000000002"));
        }

        @Test
        @DisplayName("Should accept Carnet de Extranjería (12 digits starting with 20) for Boleta")
        void testCreateBoletaConCarnetExtranjeria() throws Exception {
            VentaCreateDTO createDTO = VentaCreateDTO.builder()
                    .clienteId(clientePrincipal.getId())
                    .metodoPago("Efectivo")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("200123456789")
                    .detalles(List.of(
                            DetalleVentaCreateDTO.builder()
                                    .productoId(prodOk.getId())
                                    .cantidad(1)
                                    .precioUnitario(50.0)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.documento_fiscal").value("200123456789"));
        }

        @Test
        @DisplayName("Should create Factura with valid 11-digit RUC and generate FA64 correlative")
        void testCreateFacturaConRucValido() throws Exception {
            VentaCreateDTO createDTO = VentaCreateDTO.builder()
                    .clienteId(clientePrincipal.getId())
                    .metodoPago("Transferencia")
                    .tipoComprobante("Factura")
                    .documentoFiscal("20123456789")
                    .detalles(List.of(
                            DetalleVentaCreateDTO.builder()
                                    .productoId(prodOk.getId())
                                    .cantidad(2)
                                    .precioUnitario(50.0)
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.serie").value("FA64"))
                    .andExpect(jsonPath("$.numero").value("0000000001"))
                    .andExpect(jsonPath("$.comprobante").value("FA64-0000000001"));
        }

        @Test
        @DisplayName("Should reject invalid fiscal documents and invalid receipt types")
        void testFiscalDocumentValidations() throws Exception {
            // Boleta with invalid DNI (not 8 or 12 digits)
            VentaCreateDTO badDni = VentaCreateDTO.builder()
                    .metodoPago("Efectivo")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("123")
                    .detalles(List.of(new DetalleVentaCreateDTO(prodOk.getId(), 1, 50.0)))
                    .build();

            mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badDni)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("DNI")));

            // Factura with invalid RUC (only 8 digits)
            VentaCreateDTO badRuc = VentaCreateDTO.builder()
                    .metodoPago("Efectivo")
                    .tipoComprobante("Factura")
                    .documentoFiscal("12345678")
                    .detalles(List.of(new DetalleVentaCreateDTO(prodOk.getId(), 1, 50.0)))
                    .build();

            mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badRuc)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("RUC")));

            // Invalid tipo_comprobante
            VentaCreateDTO badTipo = VentaCreateDTO.builder()
                    .metodoPago("Efectivo")
                    .tipoComprobante("Ticket")
                    .documentoFiscal("12345678")
                    .detalles(List.of(new DetalleVentaCreateDTO(prodOk.getId(), 1, 50.0)))
                    .build();

            mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badTipo)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("Tipo de comprobante")));
        }
    }

    @Nested
    @DisplayName("2. Stock, Expiration & Warehouse State Validations with Transactional Rollback")
    class StockAndExpiryValidationTests {

        @Test
        @DisplayName("Should reject sale when product has insufficient stock and rollback atomically")
        void testInsufficientStockRollback() throws Exception {
            VentaCreateDTO payload = VentaCreateDTO.builder()
                    .metodoPago("Tarjeta")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .detalles(List.of(
                            new DetalleVentaCreateDTO(prodOk.getId(), 2, 50.0),
                            new DetalleVentaCreateDTO(prodPocoStock.getId(), 5, 25.0) // only 2 in stock!
                    ))
                    .build();

            mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("Stock insuficiente")));

            // Verify prodOk stock was NOT decremented
            Producto p = productoRepository.findById(prodOk.getId()).orElseThrow();
            assertEquals(20, p.getStock());
            assertEquals(0, ventaRepository.count());
            assertEquals(0, movimientoStockRepository.count());
        }

        @Test
        @DisplayName("Should reject sale of expired product")
        void testExpiredProductRejection() throws Exception {
            Producto vencido = new Producto();
            vencido.setSku("PROD-VENCIDO");
            vencido.setNombre("Vacuna Caducada");
            vencido.setPrecio(30.0);
            vencido.setStock(10);
            vencido.setFechaVencimiento(LocalDate.now().minusDays(2));
            vencido.setEstadoAlmacen(EstadoAlmacen.DISPONIBLE);
            vencido = productoRepository.save(vencido);
            stockAlmacenRepository.save(new StockAlmacen(null, vencido, "Disponible", 10));

            VentaCreateDTO payload = VentaCreateDTO.builder()
                    .metodoPago("Efectivo")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .detalles(List.of(new DetalleVentaCreateDTO(vencido.getId(), 1, 30.0)))
                    .build();

            mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("vencido")));
        }

        @Test
        @DisplayName("Should reject sale when product is not in DISPONIBLE warehouse state")
        void testNonAvailableWarehouseStateRejection() throws Exception {
            Producto noDisp = new Producto();
            noDisp.setSku("PROD-NODISP");
            noDisp.setNombre("Producto Cuarentena");
            noDisp.setPrecio(40.0);
            noDisp.setStock(10);
            noDisp.setEstadoAlmacen(EstadoAlmacen.NO_DISPONIBLE);
            noDisp = productoRepository.save(noDisp);
            stockAlmacenRepository.save(new StockAlmacen(null, noDisp, "No disponible", 10));

            VentaCreateDTO payload = VentaCreateDTO.builder()
                    .metodoPago("Efectivo")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .detalles(List.of(new DetalleVentaCreateDTO(noDisp.getId(), 1, 40.0)))
                    .build();

            mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("no puede venderse")));
        }
    }

    @Nested
    @DisplayName("3. NotaCredito Redemption Tests")
    class NotaCreditoRedemptionTests {

        @Test
        @DisplayName("Should apply credit note partial or full redemption on sale")
        void testRedeemCreditNote() throws Exception {
            // Create active credit note for clientePrincipal with saldo 40.0
            NotaCredito nc = new NotaCredito();
            nc.setCodigo("NC-TEST01");
            nc.setCliente(clientePrincipal);
            nc.setMonto(40.0);
            nc.setSaldo(40.0);
            nc.setEstado("ACTIVA");
            nc.setFechaEmision(LocalDateTime.now());
            nc.setFechaVencimiento(LocalDateTime.now().plusDays(180));
            nc = notaCreditoRepository.save(nc);

            // Buy 1 prodOk for 50.0 using nc
            VentaCreateDTO payload = VentaCreateDTO.builder()
                    .clienteId(clientePrincipal.getId())
                    .metodoPago("Efectivo")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .notaCreditoId(nc.getId())
                    .detalles(List.of(new DetalleVentaCreateDTO(prodOk.getId(), 1, 50.0)))
                    .build();

            mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(10.0))
                    .andExpect(jsonPath("$.monto_nota").value(40.0))
                    .andExpect(jsonPath("$.nota_credito_id").value(nc.getId()));

            NotaCredito updatedNc = notaCreditoRepository.findById(nc.getId()).orElseThrow();
            assertEquals(0.0, updatedNc.getSaldo());
            assertEquals("CANJEADA", updatedNc.getEstado());
        }

        @Test
        @DisplayName("Should reject redeeming another client's or expired credit note")
        void testInvalidCreditNoteRedemption() throws Exception {
            // Belongs to otroCliente
            NotaCredito ncOtro = new NotaCredito();
            ncOtro.setCodigo("NC-OTRO01");
            ncOtro.setCliente(otroCliente);
            ncOtro.setMonto(20.0);
            ncOtro.setSaldo(20.0);
            ncOtro.setEstado("ACTIVA");
            ncOtro.setFechaEmision(LocalDateTime.now());
            ncOtro.setFechaVencimiento(LocalDateTime.now().plusDays(180));
            ncOtro = notaCreditoRepository.save(ncOtro);

            VentaCreateDTO payload = VentaCreateDTO.builder()
                    .clienteId(clientePrincipal.getId())
                    .metodoPago("Efectivo")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .notaCreditoId(ncOtro.getId())
                    .detalles(List.of(new DetalleVentaCreateDTO(prodOk.getId(), 1, 50.0)))
                    .build();

            mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("otro cliente")));
        }
    }

    @Nested
    @DisplayName("4. Client Portal Self-Service, Ownership & Access Isolation Tests")
    class ClientPortalVentaTests {

        @Test
        @DisplayName("Should automatically bind cliente_id when role is Cliente")
        void testClienteAutoBindsClienteId() throws Exception {
            VentaCreateDTO payload = VentaCreateDTO.builder()
                    .metodoPago("Tarjeta")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .detalles(List.of(new DetalleVentaCreateDTO(prodOk.getId(), 1, 50.0)))
                    .build();

            MvcResult res = mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cliente_id").value(clientePrincipal.getId()))
                    .andExpect(jsonPath("$.cliente_nombre").value("Cliente Principal"))
                    .andReturn();

            JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
            long ventaId = node.get("id").asLong();

            // Cliente views own sales
            mockMvc.perform(get("/api/ventas/mias")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id").value(ventaId));

            // Cliente views single sale
            mockMvc.perform(get("/api/ventas/" + ventaId)
                            .header("Authorization", "Bearer " + tokenFor(clienteUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ventaId));

            // Otro cliente attempts to view -> 403 Forbidden
            mockMvc.perform(get("/api/ventas/" + ventaId)
                            .header("Authorization", "Bearer " + tokenFor(otroClienteUser)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail", containsString("No tienes acceso a esta venta")));
        }
    }

    @Nested
    @DisplayName("5. Cancellation Request, State Transitions & Stock Reversal Tests")
    class StateTransitionsAndStockReversalTests {

        @Test
        @DisplayName("Should allow Cliente to request cancellation on Pendiente or Confirmada sale")
        void testSolicitarAnulacionCliente() throws Exception {
            VentaCreateDTO payload = VentaCreateDTO.builder()
                    .metodoPago("Tarjeta")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .detalles(List.of(new DetalleVentaCreateDTO(prodOk.getId(), 2, 50.0)))
                    .build();

            MvcResult res = mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andReturn();

            long ventaId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

            // Request cancellation by owner
            mockMvc.perform(post("/api/ventas/" + ventaId + "/solicitar-anulacion")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("Anulación solicitada"));

            // Admin confirms cancellation -> stock restored
            mockMvc.perform(put("/api/ventas/" + ventaId + "/estado?estado=Cancelada")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("Cancelada"));

            // Stock restored to 20
            Producto p = productoRepository.findById(prodOk.getId()).orElseThrow();
            assertEquals(20, p.getStock());

            // ANULACION_VENTA movement logged
            List<MovimientoStock> movs = movimientoStockRepository.findAll();
            assertTrue(movs.stream().anyMatch(m -> "ANULACION_VENTA".equals(m.getTipoMovimiento()) && m.getCantidad() == 2));
        }

        @Test
        @DisplayName("Should validate permitted transitions and reject illegal transitions")
        void testStateTransitions() throws Exception {
            VentaCreateDTO payload = VentaCreateDTO.builder()
                    .clienteId(clientePrincipal.getId())
                    .metodoPago("Efectivo")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .detalles(List.of(new DetalleVentaCreateDTO(prodOk.getId(), 1, 50.0)))
                    .build();

            MvcResult res = mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andReturn();

            long ventaId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

            // Pendiente -> Entregada is invalid (must go through Confirmada)
            mockMvc.perform(put("/api/ventas/" + ventaId + "/estado?estado=Entregada")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isBadRequest());

            // Pendiente -> Confirmada
            mockMvc.perform(put("/api/ventas/" + ventaId + "/estado?estado=Confirmada")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("Confirmada"));

            // Confirmada -> Entregada
            mockMvc.perform(put("/api/ventas/" + ventaId + "/estado?estado=Entregada")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado").value("Entregada"));

            // Entregada is terminal: cannot transition further
            mockMvc.perform(put("/api/ventas/" + ventaId + "/estado?estado=Cancelada")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("6. Post-Delivery Returns (Devolución) & Credit Note Issuance Tests")
    class DevolucionAndNotaCreditoTests {

        @Test
        @DisplayName("Should return delivered sale via EXTORNO and restore stock")
        void testDevolucionExtorno() throws Exception {
            VentaCreateDTO payload = VentaCreateDTO.builder()
                    .clienteId(clientePrincipal.getId())
                    .metodoPago("Efectivo")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .detalles(List.of(new DetalleVentaCreateDTO(prodOk.getId(), 3, 50.0)))
                    .build();

            MvcResult res = mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andReturn();

            long ventaId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

            // Deliver sale
            mockMvc.perform(put("/api/ventas/" + ventaId + "/estado?estado=Confirmada")
                    .header("Authorization", "Bearer " + tokenFor(adminUser)));
            mockMvc.perform(put("/api/ventas/" + ventaId + "/estado?estado=Entregada")
                    .header("Authorization", "Bearer " + tokenFor(adminUser)));

            // Devolver via EXTORNO
            DevolucionRequestDTO req = DevolucionRequestDTO.builder()
                    .tipo("EXTORNO")
                    .motivo("Producto con empaque golpeado")
                    .build();

            mockMvc.perform(post("/api/ventas/" + ventaId + "/devolver")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            // Stock restored to 20
            Producto p = productoRepository.findById(prodOk.getId()).orElseThrow();
            assertEquals(20, p.getStock());

            // Check GET /api/devoluciones
            mockMvc.perform(get("/api/devoluciones")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].tipo").value("EXTORNO"))
                    .andExpect(jsonPath("$[0].monto").value(150.0))
                    .andExpect(jsonPath("$[0].venta_id").value(ventaId));
        }

        @Test
        @DisplayName("Should return delivered sale via NOTA_CREDITO and issue 180-day NC-XXXXXX")
        void testDevolucionNotaCredito() throws Exception {
            VentaCreateDTO payload = VentaCreateDTO.builder()
                    .clienteId(clientePrincipal.getId())
                    .metodoPago("Tarjeta")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .detalles(List.of(new DetalleVentaCreateDTO(prodOk.getId(), 2, 50.0)))
                    .build();

            MvcResult res = mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andReturn();

            long ventaId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

            // Deliver sale
            mockMvc.perform(put("/api/ventas/" + ventaId + "/estado?estado=Confirmada")
                    .header("Authorization", "Bearer " + tokenFor(adminUser)));
            mockMvc.perform(put("/api/ventas/" + ventaId + "/estado?estado=Entregada")
                    .header("Authorization", "Bearer " + tokenFor(adminUser)));

            // Devolver via NOTA_CREDITO
            DevolucionRequestDTO req = DevolucionRequestDTO.builder()
                    .tipo("NOTA_CREDITO")
                    .motivo("Cambio por crédito en tienda")
                    .build();

            mockMvc.perform(post("/api/ventas/" + ventaId + "/devolver")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            // GET /api/notas-credito as admin
            mockMvc.perform(get("/api/notas-credito")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].codigo", startsWith("NC-")))
                    .andExpect(jsonPath("$[0].monto").value(100.0))
                    .andExpect(jsonPath("$[0].saldo").value(100.0))
                    .andExpect(jsonPath("$[0].estado").value("ACTIVA"))
                    .andExpect(jsonPath("$[0].vigente").value(true))
                    .andExpect(jsonPath("$[0].cliente_nombre").value("Cliente Principal"));

            // GET /api/notas-credito as owner Cliente
            mockMvc.perform(get("/api/notas-credito")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].cliente_id").value(clientePrincipal.getId()));

            // GET /api/notas-credito as other Cliente -> empty list
            mockMvc.perform(get("/api/notas-credito")
                            .header("Authorization", "Bearer " + tokenFor(otroClienteUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should reject return of non-delivered sale")
        void testReturnNonDeliveredSaleRejected() throws Exception {
            VentaCreateDTO payload = VentaCreateDTO.builder()
                    .clienteId(clientePrincipal.getId())
                    .metodoPago("Efectivo")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .detalles(List.of(new DetalleVentaCreateDTO(prodOk.getId(), 1, 50.0)))
                    .build();

            MvcResult res = mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isOk())
                    .andReturn();

            long ventaId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

            DevolucionRequestDTO req = DevolucionRequestDTO.builder()
                    .tipo("EXTORNO")
                    .motivo("Devolución prematura")
                    .build();

            mockMvc.perform(post("/api/ventas/" + ventaId + "/devolver")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("entregadas")));
        }

        @Test
        @DisplayName("Cannot return an already returned sale twice (idempotency guard)")
        void testCannotReturnSaleTwice() throws Exception {
            VentaCreateDTO ventaReq = VentaCreateDTO.builder()
                    .clienteId(clientePrincipal.getId())
                    .metodoPago("EFECTIVO")
                    .tipoComprobante("Boleta")
                    .documentoFiscal("12345678")
                    .detalles(List.of(new DetalleVentaCreateDTO(prodOk.getId(), 1, 50.0)))
                    .build();

            MvcResult res = mockMvc.perform(post("/api/ventas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ventaReq)))
                    .andExpect(status().isOk())
                    .andReturn();

            long ventaId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

            // Set to Confirmada then Entregada
            mockMvc.perform(put("/api/ventas/" + ventaId + "/estado?estado=Confirmada")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk());
            mockMvc.perform(put("/api/ventas/" + ventaId + "/estado?estado=Entregada")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk());

            DevolucionRequestDTO req = DevolucionRequestDTO.builder()
                    .tipo("EXTORNO")
                    .motivo("Defecto de fábrica")
                    .build();

            // First return succeeds
            mockMvc.perform(post("/api/ventas/" + ventaId + "/devolver")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            // Second return fails with 400 Bad Request
            mockMvc.perform(post("/api/ventas/" + ventaId + "/devolver")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("ya cuenta con una devolución registrada")));
        }
    }
}
