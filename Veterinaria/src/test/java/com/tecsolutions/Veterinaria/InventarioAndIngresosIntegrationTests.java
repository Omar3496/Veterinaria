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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class InventarioAndIngresosIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

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
    private IngresoRepository ingresoRepository;

    @Autowired
    private IngresoDetalleRepository ingresoDetalleRepository;

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        rateLimitingFilter.reset();

        ingresoDetalleRepository.deleteAll();
        ingresoRepository.deleteAll();
        movimientoStockRepository.deleteAll();
        stockAlmacenRepository.deleteAll();
        productoRepository.deleteAll();
        proveedorRepository.deleteAll();
        categoriaRepository.deleteAll();
        familiaRepository.deleteAll();
        marcaRepository.deleteAll();
        auditoriaRepository.deleteAll();
        clienteRepository.deleteAll();
        usuarioRepository.deleteAll();

        adminUser = new Usuario(null, "admin", passwordEncoder.encode("admin123"), RolEnum.ADMIN, true, "admin@sirbull.pe");
        adminUser = usuarioRepository.save(adminUser);

        recepUser = new Usuario(null, "recep", passwordEncoder.encode("recep123"), RolEnum.RECEPCIONISTA, true, "recep@sirbull.pe");
        recepUser = usuarioRepository.save(recepUser);

        vetUser = new Usuario(null, "vet", passwordEncoder.encode("vet123"), RolEnum.VETERINARIO, true, "vet@sirbull.pe");
        vetUser = usuarioRepository.save(vetUser);

        clienteUser = new Usuario(null, "cliente", passwordEncoder.encode("cliente123"), RolEnum.CLIENTE, true, "cliente@sirbull.pe");
        clienteUser = usuarioRepository.save(clienteUser);
    }

    @AfterEach
    void tearDown() {
        ingresoDetalleRepository.deleteAll();
        ingresoRepository.deleteAll();
        movimientoStockRepository.deleteAll();
        stockAlmacenRepository.deleteAll();
        productoRepository.deleteAll();
        proveedorRepository.deleteAll();
        categoriaRepository.deleteAll();
        familiaRepository.deleteAll();
        marcaRepository.deleteAll();
        auditoriaRepository.deleteAll();
        clienteRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    private String tokenFor(Usuario usuario) {
        return jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().getValue());
    }

    @Nested
    @DisplayName("1. Catalog CRUD & Duplicate Validation Tests")
    class CatalogCRUDTests {

        @Test
        @DisplayName("CRUD Categoria with duplicate detection (409)")
        void categoriaCrudAndDuplicateCheck() throws Exception {
            String adminToken = tokenFor(adminUser);
            String recepToken = tokenFor(recepUser);

            // Create category as Admin
            CategoriaDTO createDto = CategoriaDTO.builder().nombre("Alimentos").build();
            MvcResult res = mockMvc.perform(post("/api/categorias")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.nombre").value("Alimentos"))
                    .andReturn();

            JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
            long catId = node.get("id").asLong();

            // Duplicate on create returns 409
            mockMvc.perform(post("/api/categorias")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("Categoría ya registrada"));

            // Recepcionista can list
            mockMvc.perform(get("/api/categorias")
                            .header("Authorization", "Bearer " + recepToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].nombre").value("Alimentos"));

            // Update as Admin
            CategoriaDTO updateDto = CategoriaDTO.builder().nombre("Nutrición").build();
            mockMvc.perform(put("/api/categorias/" + catId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Nutrición"));

            // Delete as Admin
            mockMvc.perform(delete("/api/categorias/" + catId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail").value("Categoría eliminada"));

            // Empty after deletion
            mockMvc.perform(get("/api/categorias")
                            .header("Authorization", "Bearer " + recepToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("CRUD Familia with duplicate detection (409)")
        void familiaCrudAndDuplicateCheck() throws Exception {
            String adminToken = tokenFor(adminUser);

            FamiliaDTO f1 = FamiliaDTO.builder().nombre("Caninos").build();
            MvcResult res = mockMvc.perform(post("/api/familias")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(f1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Caninos"))
                    .andReturn();

            long famId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

            // Duplicate 409
            mockMvc.perform(post("/api/familias")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(f1)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("Familia ya registrada"));

            // Delete
            mockMvc.perform(delete("/api/familias/" + famId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail").value("Familia eliminada"));
        }

        @Test
        @DisplayName("CRUD Marca with duplicate detection (409)")
        void marcaCrudAndDuplicateCheck() throws Exception {
            String adminToken = tokenFor(adminUser);

            MarcaDTO m1 = MarcaDTO.builder().nombre("Royal Canin").build();
            MvcResult res = mockMvc.perform(post("/api/marcas")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(m1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nombre").value("Royal Canin"))
                    .andReturn();

            long marcaId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

            // Duplicate 409
            mockMvc.perform(post("/api/marcas")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(m1)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("Marca ya registrada"));

            // Delete
            mockMvc.perform(delete("/api/marcas/" + marcaId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail").value("Marca eliminada"));
        }

        @Test
        @DisplayName("CRUD Proveedor with duplicate RUC detection (409)")
        void proveedorCrudAndDuplicateCheck() throws Exception {
            String adminToken = tokenFor(adminUser);

            ProveedorDTO p1 = ProveedorDTO.builder()
                    .ruc("20123456789")
                    .razonSocial("Distribuidora Veterinaria SAC")
                    .contacto("Carlos Ruiz")
                    .telefono("999888777")
                    .direccion("Av. Colonial 123")
                    .email("contacto@distrivet.pe")
                    .moneda("SOL")
                    .build();

            MvcResult res = mockMvc.perform(post("/api/proveedores")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(p1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.razon_social").value("Distribuidora Veterinaria SAC"))
                    .andExpect(jsonPath("$.ruc").value("20123456789"))
                    .andReturn();

            long provId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

            // Duplicate RUC 409
            mockMvc.perform(post("/api/proveedores")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(p1)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("El RUC ya está registrado"));

            // Delete
            mockMvc.perform(delete("/api/proveedores/" + provId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail").value("Proveedor eliminado"));
        }
    }

    @Nested
    @DisplayName("2. SKU Generation Tests")
    class SkuGenerationTests {

        @Test
        @DisplayName("Generates 20260001 when catalog is empty and increments sequentially")
        void nextSkuGeneration() throws Exception {
            String adminToken = tokenFor(adminUser);
            String recepToken = tokenFor(recepUser);

            // Empty catalog -> 20260001
            mockMvc.perform(get("/api/productos/next-sku")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sku").value("20260001"));

            // Recepcionista can also request next-sku
            mockMvc.perform(get("/api/productos/next-sku")
                            .header("Authorization", "Bearer " + recepToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sku").value("20260001"));

            // Insert a product with SKU 20260001
            Producto p = new Producto();
            p.setSku("20260001");
            p.setNombre("Alimento Seco 1kg");
            p.setPrecio(25.0);
            p.setStock(10);
            productoRepository.save(p);

            // Next SKU is now 20260002
            mockMvc.perform(get("/api/productos/next-sku")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sku").value("20260002"));
        }
    }

    @Nested
    @DisplayName("3. Public Products Catalog & Anonymous Access Tests")
    class PublicProductosTests {

        @Test
        @DisplayName("GET /api/productos is public and works without token")
        void publicCatalogAccess() throws Exception {
            Producto p = new Producto();
            p.setSku("20260010");
            p.setNombre("Shampoo Hipoalergénico");
            p.setPrecio(35.5);
            p.setStock(8);
            productoRepository.save(p);

            mockMvc.perform(get("/api/productos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].sku").value("20260010"))
                    .andExpect(jsonPath("$[0].nombre").value("Shampoo Hipoalergénico"))
                    .andExpect(jsonPath("$[0].precio").value(35.5));
        }
    }

    @Nested
    @DisplayName("4. Producto CRUD & StockAlmacen Initialization Tests")
    class ProductoCRUDTests {

        @Test
        @DisplayName("Creating product auto-generates SKU, sets relations, and initializes StockAlmacen")
        void createProductWithStockAlmacenInit() throws Exception {
            String adminToken = tokenFor(adminUser);

            Proveedor prov = proveedorRepository.save(new Proveedor(null, "20999888777", "VetPeru", "Ana", "987", "Dir", "a@v.pe", "SOL", null, null));
            Categoria cat = categoriaRepository.save(new Categoria(null, "Farmacia", null));
            Familia fam = familiaRepository.save(new Familia(null, "Antibióticos", null));
            Marca mar = marcaRepository.save(new Marca(null, "Bayer", null));

            ProductoDTO dto = ProductoDTO.builder()
                    .nombre("Amoxicilina 250mg")
                    .descripcion("Antibiótico de amplio espectro")
                    .precio(45.0)
                    .stock(20)
                    .stockMinimo(5)
                    .precioCompra(30.0)
                    .estadoAlmacen("Disponible")
                    .proveedorId(prov.getId())
                    .categoriaId(cat.getId())
                    .familiaId(fam.getId())
                    .marcaId(mar.getId())
                    .build();

            MvcResult res = mockMvc.perform(post("/api/productos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.sku").value("20260001"))
                    .andExpect(jsonPath("$.proveedor_nombre").value("VetPeru"))
                    .andExpect(jsonPath("$.categoria_nombre").value("Farmacia"))
                    .andExpect(jsonPath("$.familia_nombre").value("Antibióticos"))
                    .andExpect(jsonPath("$.marca_nombre").value("Bayer"))
                    .andExpect(jsonPath("$.estado_almacen").value("Disponible"))
                    .andExpect(jsonPath("$.estado_almacen_nombre").value("Disponible"))
                    .andExpect(jsonPath("$.stock_por_almacen", hasSize(1)))
                    .andExpect(jsonPath("$.stock_por_almacen[0].almacen").value("Disponible"))
                    .andExpect(jsonPath("$.stock_por_almacen[0].cantidad").value(20))
                    .andReturn();

            long prodId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

            // Duplicate SKU rejects with 409
            dto.setSku("20260001");
            mockMvc.perform(post("/api/productos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("El SKU ya está en uso"));

            // Update product stock from 20 to 25 -> triggers AJUSTE MovimientoStock
            dto.setStock(25);
            mockMvc.perform(put("/api/productos/" + prodId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.stock").value(25))
                    .andExpect(jsonPath("$.stock_por_almacen[0].cantidad").value(25));

            List<MovimientoStock> movimientos = movimientoStockRepository.findAll();
            assertFalse(movimientos.isEmpty());
            MovimientoStock ajuste = movimientos.getFirst();
            assertEquals("AJUSTE", ajuste.getTipoMovimiento());
            assertEquals(5, ajuste.getCantidad());
            assertEquals("Ajuste de stock desde edición de producto", ajuste.getObservacion());
        }
    }

    @Nested
    @DisplayName("5. Stock Adjustments & Kardex Logging Tests")
    class StockAdjustmentTests {

        @Test
        @DisplayName("PUT /api/productos/{id}/stock supports sumar and restar with validation")
        void updateStockSumarAndRestar() throws Exception {
            String adminToken = tokenFor(adminUser);
            String recepToken = tokenFor(recepUser);

            Producto p = new Producto();
            p.setSku("20260050");
            p.setNombre("Antiparasitario");
            p.setPrecio(15.0);
            p.setStock(10);
            p.setEstadoAlmacen(EstadoAlmacen.DISPONIBLE);
            p = productoRepository.save(p);
            stockAlmacenRepository.save(new StockAlmacen(null, p, "Disponible", 10));

            // Sumar 5 as Recepcionista
            mockMvc.perform(put("/api/productos/" + p.getId() + "/stock?cantidad=5&operacion=sumar")
                            .header("Authorization", "Bearer " + recepToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.stock").value(15))
                    .andExpect(jsonPath("$.stock_por_almacen[0].cantidad").value(15));

            // Restar 4 as Admin
            mockMvc.perform(put("/api/productos/" + p.getId() + "/stock?cantidad=4&operacion=restar")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.stock").value(11))
                    .andExpect(jsonPath("$.stock_por_almacen[0].cantidad").value(11));

            // Restar 20 (more than available) -> 400 Stock insuficiente
            mockMvc.perform(put("/api/productos/" + p.getId() + "/stock?cantidad=20&operacion=restar")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Stock insuficiente"));
        }
    }

    @Nested
    @DisplayName("6. Warehouse State Transition & Stock Movement Tests")
    class WarehouseStateAndMovementTests {

        @Test
        @DisplayName("PUT /api/productos/{id}/estado-almacen moves full stock and updates product state")
        void cambiarEstadoAlmacenFullTransfer() throws Exception {
            String adminToken = tokenFor(adminUser);

            Producto p = new Producto();
            p.setSku("20260060");
            p.setNombre("Vacuna Séxtuple");
            p.setPrecio(60.0);
            p.setStock(12);
            p.setEstadoAlmacen(EstadoAlmacen.DISPONIBLE);
            p = productoRepository.save(p);
            stockAlmacenRepository.save(new StockAlmacen(null, p, "Disponible", 12));

            CambioEstadoAlmacenRequestDTO req = CambioEstadoAlmacenRequestDTO.builder()
                    .estado("No disponible")
                    .observacion("Lote bajo cuarentena")
                    .build();

            mockMvc.perform(put("/api/productos/" + p.getId() + "/estado-almacen")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado_almacen").value("No disponible"))
                    .andExpect(jsonPath("$.stock_por_almacen", hasSize(1)))
                    .andExpect(jsonPath("$.stock_por_almacen[0].almacen").value("No disponible"))
                    .andExpect(jsonPath("$.stock_por_almacen[0].cantidad").value(12));

            // Repeating the same state returns 400
            mockMvc.perform(put("/api/productos/" + p.getId() + "/estado-almacen")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("El producto ya se encuentra en ese estado"));
        }

        @Test
        @DisplayName("POST /api/almacenes/mover transfers partial quantities between warehouses")
        void moverStockEntreAlmacenes() throws Exception {
            String recepToken = tokenFor(recepUser);

            Producto p = new Producto();
            p.setSku("20260070");
            p.setNombre("Colirio Oftálmico");
            p.setPrecio(20.0);
            p.setStock(10);
            p.setEstadoAlmacen(EstadoAlmacen.DISPONIBLE);
            p = productoRepository.save(p);
            stockAlmacenRepository.save(new StockAlmacen(null, p, "Disponible", 10));

            MovimientoAlmacenRequestDTO req = MovimientoAlmacenRequestDTO.builder()
                    .productoId(p.getId())
                    .almacenOrigen("Disponible")
                    .almacenDestino("Dañado")
                    .cantidad(3)
                    .observacion("Frasco roto durante traslado")
                    .build();

            mockMvc.perform(post("/api/almacenes/mover")
                            .header("Authorization", "Bearer " + recepToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.stock").value(10))
                    .andExpect(jsonPath("$.stock_por_almacen", hasSize(2)));

            // Insufficient stock in origin returns 400
            req.setCantidad(20);
            mockMvc.perform(post("/api/almacenes/mover")
                            .header("Authorization", "Bearer " + recepToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("Stock insuficiente en 'Disponible'")));

            // Same origin and destination returns 400
            req.setAlmacenDestino("Disponible");
            req.setCantidad(2);
            mockMvc.perform(post("/api/almacenes/mover")
                            .header("Authorization", "Bearer " + recepToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("El almacén de origen y destino deben ser distintos"));
        }
    }

    @Nested
    @DisplayName("7. Kardex MovimientosStock Listing Tests")
    class KardexMovimientosStockTests {

        @Test
        @DisplayName("GET /api/movimientos-stock lists kardex entries for staff, forbidden for client")
        void listarMovimientosStock() throws Exception {
            String adminToken = tokenFor(adminUser);
            String recepToken = tokenFor(recepUser);
            String clienteToken = tokenFor(clienteUser);

            Producto p = new Producto();
            p.setSku("20260080");
            p.setNombre("Gasas Estériles");
            p.setPrecio(5.0);
            p.setStock(50);
            p = productoRepository.save(p);

            MovimientoStock mov = new MovimientoStock(null, p, java.time.LocalDateTime.now(), "INGRESO", 50, null, "Disponible", "Ingreso inicial", null, adminUser);
            movimientoStockRepository.save(mov);

            // Admin can list
            mockMvc.perform(get("/api/movimientos-stock")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].tipo_movimiento").value("INGRESO"))
                    .andExpect(jsonPath("$[0].producto_nombre").value("Gasas Estériles"))
                    .andExpect(jsonPath("$[0].usuario_nombre").value("admin"));

            // Recep can list
            mockMvc.perform(get("/api/movimientos-stock")
                            .header("Authorization", "Bearer " + recepToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            // Cliente is forbidden (403)
            mockMvc.perform(get("/api/movimientos-stock")
                            .header("Authorization", "Bearer " + clienteToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("No tienes permisos suficientes para realizar esta acción"));
        }
    }

    @Nested
    @DisplayName("8. Ingresos (Compras de Mercadería) & Annulment Tests")
    class IngresosComprasTests {

        @Test
        @DisplayName("POST /api/ingresos records purchase, updates stock & prices, and DELETE /api/ingresos/{id} reverts stock")
        void createAndAnnulIngreso() throws Exception {
            String adminToken = tokenFor(adminUser);
            String recepToken = tokenFor(recepUser);

            Proveedor prov = proveedorRepository.save(new Proveedor(null, "20555666777", "Farmavet SA", "Pedro", "911222333", "Lima", "f@v.pe", "SOL", null, null));
            Producto prod = new Producto();
            prod.setSku("20260090");
            prod.setNombre("Suero Fisiológico 500ml");
            prod.setPrecio(10.0);
            prod.setStock(5);
            prod.setPrecioCompra(6.0);
            prod = productoRepository.save(prod);
            stockAlmacenRepository.save(new StockAlmacen(null, prod, "Disponible", 5));

            IngresoDetalleDTO detDto = IngresoDetalleDTO.builder()
                    .productoId(prod.getId())
                    .cantidad(10)
                    .precioUnitario(7.5)
                    .precioVenta(12.0)
                    .fechaVencimiento(LocalDate.now().plusMonths(6))
                    .build();

            IngresoDTO ingresoDto = IngresoDTO.builder()
                    .proveedorId(prov.getId())
                    .tipoPago("Contado")
                    .tipoComprobante("Factura")
                    .serie("F001")
                    .numero("000123")
                    .detalles(List.of(detDto))
                    .build();

            // Recepcionista can register merchandise receipt
            MvcResult res = mockMvc.perform(post("/api/ingresos")
                            .header("Authorization", "Bearer " + recepToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ingresoDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.numero_factura").value("F001-000123"))
                    .andExpect(jsonPath("$.total").value(75.0))
                    .andExpect(jsonPath("$.proveedor_nombre").value("Farmavet SA"))
                    .andExpect(jsonPath("$.detalles", hasSize(1)))
                    .andExpect(jsonPath("$.detalles[0].subtotal").value(75.0))
                    .andReturn();

            long ingresoId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong();

            // Verify product stock and prices updated
            Producto updatedProd = productoRepository.findById(prod.getId()).orElseThrow();
            assertEquals(15, updatedProd.getStock());
            assertEquals(7.5, updatedProd.getPrecioCompra());
            assertEquals(12.0, updatedProd.getPrecio());
            assertTrue(updatedProd.getEsPerecible());
            assertNotNull(updatedProd.getFechaVencimiento());

            // Check kardex INGRESO log
            List<MovimientoStock> kardex = movimientoStockRepository.findAll();
            assertTrue(kardex.stream().anyMatch(m -> "INGRESO".equals(m.getTipoMovimiento()) && m.getCantidad() == 10));

            // List receipts
            mockMvc.perform(get("/api/ingresos")
                            .header("Authorization", "Bearer " + recepToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].total").value(75.0));

            // Annul receipt as Admin (DELETE /api/ingresos/{id})
            mockMvc.perform(delete("/api/ingresos/" + ingresoId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail").value("Ingreso eliminado y stock revertido"));

            // Stock reverted from 15 back to 5
            Producto revertedProd = productoRepository.findById(prod.getId()).orElseThrow();
            assertEquals(5, revertedProd.getStock());

            // ANULACION_INGRESO logged
            List<MovimientoStock> updatedKardex = movimientoStockRepository.findAll();
            assertTrue(updatedKardex.stream().anyMatch(m -> "ANULACION_INGRESO".equals(m.getTipoMovimiento()) && m.getCantidad() == -10));

            // Repeating delete on already annulled ingreso fails -> 404 Not Found (Idempotency guard)
            mockMvc.perform(delete("/api/ingresos/" + ingresoId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Ingreso no encontrado"));
        }

        @Test
        @DisplayName("POST /api/ingresos rejects past expiration date (400)")
        void pastExpirationDateRejected() throws Exception {
            String adminToken = tokenFor(adminUser);

            Proveedor prov = proveedorRepository.save(new Proveedor(null, "20111222333", "Droguería SA", "Carlos", "999", "Lima", "d@s.pe", "SOL", null, null));
            Producto prod = new Producto();
            prod.setSku("20260095");
            prod.setNombre("Jeringas 5ml");
            prod.setPrecio(2.0);
            prod.setStock(10);
            prod = productoRepository.save(prod);

            IngresoDetalleDTO detDto = IngresoDetalleDTO.builder()
                    .productoId(prod.getId())
                    .cantidad(5)
                    .precioUnitario(1.0)
                    .fechaVencimiento(LocalDate.now().minusDays(1)) // Expired
                    .build();

            IngresoDTO ingresoDto = IngresoDTO.builder()
                    .proveedorId(prov.getId())
                    .detalles(List.of(detDto))
                    .build();

            mockMvc.perform(post("/api/ingresos")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ingresoDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("no puede ser anterior a hoy")));
        }
    }

    @Nested
    @DisplayName("9. RBAC & Security Matrix Tests")
    class RBACSecurityTests {

        @Test
        @DisplayName("Recepcionista cannot create, update, or delete products; Cliente cannot access staff endpoints")
        void rbacEnforcement() throws Exception {
            String recepToken = tokenFor(recepUser);
            String clienteToken = tokenFor(clienteUser);

            ProductoDTO prodDto = ProductoDTO.builder().nombre("Producto X").precio(10.0).stock(5).build();

            // Recepcionista cannot create product -> 403
            mockMvc.perform(post("/api/productos")
                            .header("Authorization", "Bearer " + recepToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(prodDto)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("No tienes permisos suficientes para realizar esta acción"));

            // Cliente cannot access /api/proveedores -> 403
            mockMvc.perform(get("/api/proveedores")
                            .header("Authorization", "Bearer " + clienteToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("No tienes permisos suficientes para realizar esta acción"));

            // Cliente cannot access /api/ingresos -> 403
            mockMvc.perform(get("/api/ingresos")
                            .header("Authorization", "Bearer " + clienteToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("No tienes permisos suficientes para realizar esta acción"));
        }
    }
}
