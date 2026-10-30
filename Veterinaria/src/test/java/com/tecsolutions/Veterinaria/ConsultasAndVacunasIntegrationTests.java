package com.tecsolutions.Veterinaria;

import com.tecsolutions.Veterinaria.dto.*;
import com.tecsolutions.Veterinaria.model.*;
import com.tecsolutions.Veterinaria.repository.*;
import com.tecsolutions.Veterinaria.security.JwtUtil;
import com.tecsolutions.Veterinaria.security.RateLimitingFilter;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ConsultasAndVacunasIntegrationTests {

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
    private RecetaMedicaRepository recetaMedicaRepository;

    @Autowired
    private OrdenExamenRepository ordenExamenRepository;

    @Autowired
    private ReferenciaMedicaRepository referenciaMedicaRepository;

    @Autowired
    private VacunaRepository vacunaRepository;

    @Autowired
    private ExamenAuxiliarRepository examenAuxiliarRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private StockAlmacenRepository stockAlmacenRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario adminUser;
    private Usuario vetUser;
    private Usuario recepUser;
    private Usuario clienteUser1;
    private Usuario clienteUser2;

    private Cliente cliente1;
    private Cliente cliente2;

    private Mascota mascota1;
    private Mascota mascota2;

    private ExamenAuxiliar examenHemograma;
    private ExamenAuxiliar examenInactivo;
    private Producto vacunaRabiaProd;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        rateLimitingFilter.reset();

        recetaMedicaRepository.deleteAll();
        referenciaMedicaRepository.deleteAll();
        ordenExamenRepository.deleteAll();
        vacunaRepository.deleteAll();
        pagoRepository.deleteAll();
        consultaRepository.deleteAll();
        citaRepository.deleteAll();
        examenAuxiliarRepository.deleteAll();
        stockAlmacenRepository.deleteAll();
        productoRepository.deleteAll();
        mascotaRepository.deleteAll();
        auditoriaRepository.deleteAll();
        clienteRepository.deleteAll();
        usuarioRepository.deleteAll();

        // 1. Staff users
        adminUser = usuarioRepository.save(new Usuario(null, "admin", passwordEncoder.encode("admin123"), RolEnum.ADMIN, true, "admin@sirbull.pe"));
        vetUser = usuarioRepository.save(new Usuario(null, "vet", passwordEncoder.encode("vet123"), RolEnum.VETERINARIO, true, "vet@sirbull.pe"));
        recepUser = usuarioRepository.save(new Usuario(null, "recep", passwordEncoder.encode("recep123"), RolEnum.RECEPCIONISTA, true, "recep@sirbull.pe"));

        // 2. Client users
        clienteUser1 = usuarioRepository.save(new Usuario(null, "cliente1", passwordEncoder.encode("pass123"), RolEnum.CLIENTE, true, "cliente1@sirbull.pe"));
        clienteUser2 = usuarioRepository.save(new Usuario(null, "cliente2", passwordEncoder.encode("pass123"), RolEnum.CLIENTE, true, "cliente2@sirbull.pe"));

        // 3. Clientes
        cliente1 = new Cliente();
        cliente1.setDni("11111111");
        cliente1.setNombre("Carlos Sanchez");
        cliente1.setTelefono("911111111");
        cliente1.setEmail("cliente1@sirbull.pe");
        cliente1.setUsuario(clienteUser1);
        cliente1 = clienteRepository.save(cliente1);

        cliente2 = new Cliente();
        cliente2.setDni("22222222");
        cliente2.setNombre("Maria Lopez");
        cliente2.setTelefono("922222222");
        cliente2.setEmail("cliente2@sirbull.pe");
        cliente2.setUsuario(clienteUser2);
        cliente2 = clienteRepository.save(cliente2);

        // 4. Mascotas
        mascota1 = new Mascota();
        mascota1.setNombre("Bobi");
        mascota1.setEspecie("Canino");
        mascota1.setRaza("Labrador");
        mascota1.setFechaNacimiento(LocalDate.of(2021, 5, 10));
        mascota1.setActivo(true);
        mascota1.setPropietario(cliente1);
        mascota1 = mascotaRepository.save(mascota1);

        mascota2 = new Mascota();
        mascota2.setNombre("Michi");
        mascota2.setEspecie("Felino");
        mascota2.setRaza("Siames");
        mascota2.setFechaNacimiento(LocalDate.of(2022, 8, 15));
        mascota2.setActivo(true);
        mascota2.setPropietario(cliente2);
        mascota2 = mascotaRepository.save(mascota2);

        // 5. Exámenes Auxiliares
        examenHemograma = new ExamenAuxiliar();
        examenHemograma.setNombre("Hemograma Completo");
        examenHemograma.setDescripcion("Laboratorio");
        examenHemograma.setPrecio(45.0);
        examenHemograma.setActivo(true);
        examenHemograma = examenAuxiliarRepository.save(examenHemograma);

        examenInactivo = new ExamenAuxiliar();
        examenInactivo.setNombre("Radiografía Inactiva");
        examenInactivo.setDescripcion("Imagenología");
        examenInactivo.setPrecio(60.0);
        examenInactivo.setActivo(false);
        examenInactivo = examenAuxiliarRepository.save(examenInactivo);

        // 6. Producto Vacuna with stock
        vacunaRabiaProd = new Producto();
        vacunaRabiaProd.setNombre("Vacuna Antirrábica Nobivac");
        vacunaRabiaProd.setSku("20260001");
        vacunaRabiaProd.setPrecio(35.0);
        vacunaRabiaProd.setPrecioCompra(18.0);
        vacunaRabiaProd.setStock(10);
        vacunaRabiaProd.setStockMinimo(2);
        vacunaRabiaProd = productoRepository.save(vacunaRabiaProd);

        StockAlmacen stockDisp = new StockAlmacen(null, vacunaRabiaProd, "Disponible", 10);
        stockAlmacenRepository.save(stockDisp);
    }

    private String tokenFor(Usuario usuario) {
        return jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().getValue());
    }

    @Nested
    @DisplayName("1. Direct Consulta Creation Tests")
    class DirectConsultaTests {

        @Test
        @DisplayName("Veterinario can create direct consulta with nested prescriptions, lab orders, and referral")
        void testCreateDirectConsultaSuccess() throws Exception {
            ConsultaCreateDTO req = ConsultaCreateDTO.builder()
                    .diagnostico("Gastroenteritis aguda")
                    .tratamiento("Suero fisiológico + antibiótico oral por 5 días")
                    .peso(12.5)
                    .recetas(List.of(
                            RecetaMedicaCreateDTO.builder()
                                    .nombreMedicamento("Amoxicilina 250mg")
                                    .dosis("1 tableta cada 12h")
                                    .cantidad("10 tabletas")
                                    .indicaciones("Administrar con comida")
                                    .build()
                    ))
                    .examenes(List.of(
                            OrdenExamenCreateDTO.builder()
                                    .examenAuxiliarId(examenHemograma.getId())
                                    .resultado("Plaquetas normales, leucocitos levemente elevados")
                                    .observaciones("Control en 7 días")
                                    .build()
                    ))
                    .referencia(ReferenciaMedicaCreateDTO.builder()
                            .centroDestino("Hospital Veterinario Central")
                            .especialidad("Gastroenterología")
                            .motivo("Evaluación ecográfica abdominal")
                            .build())
                    .build();

            mockMvc.perform(post("/api/mascotas/" + mascota1.getId() + "/consultas")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.diagnostico", is("Gastroenteritis aguda")))
                    .andExpect(jsonPath("$.tratamiento", is("Suero fisiológico + antibiótico oral por 5 días")))
                    .andExpect(jsonPath("$.peso", is(12.5)))
                    .andExpect(jsonPath("$.mascota_id", is(mascota1.getId().intValue())))
                    .andExpect(jsonPath("$.veterinario_id", is(vetUser.getId().intValue())))
                    .andExpect(jsonPath("$.recetas", hasSize(1)))
                    .andExpect(jsonPath("$.recetas[0].nombre_medicamento", is("Amoxicilina 250mg")))
                    .andExpect(jsonPath("$.examenes", hasSize(1)))
                    .andExpect(jsonPath("$.examenes[0].examen_auxiliar_id", is(examenHemograma.getId().intValue())))
                    .andExpect(jsonPath("$.examenes[0].estado", is("REALIZADO")))
                    .andExpect(jsonPath("$.referencias", hasSize(1)))
                    .andExpect(jsonPath("$.referencias[0].centro_destino", is("Hospital Veterinario Central")));
        }

        @Test
        @DisplayName("Order examen with empty result gets status PENDIENTE")
        void testCreateConsultaPendingExam() throws Exception {
            ConsultaCreateDTO req = ConsultaCreateDTO.builder()
                    .diagnostico("Chequeo general")
                    .tratamiento("Ninguno")
                    .peso(11.0)
                    .examenes(List.of(
                            OrdenExamenCreateDTO.builder()
                                    .examenAuxiliarId(examenHemograma.getId())
                                    .resultado("")
                                    .observaciones("Muestra tomada")
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/api/mascotas/" + mascota1.getId() + "/consultas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.examenes[0].estado", is("PENDIENTE")));
        }

        @Test
        @DisplayName("Rejects consulta with invalid or inactive examen auxiliar (HTTP 400)")
        void testCreateConsultaInactiveExam() throws Exception {
            ConsultaCreateDTO req = ConsultaCreateDTO.builder()
                    .diagnostico("Consulta con examen inactivo")
                    .tratamiento("Tto")
                    .examenes(List.of(
                            OrdenExamenCreateDTO.builder()
                                    .examenAuxiliarId(examenInactivo.getId())
                                    .build()
                    ))
                    .build();

            mockMvc.perform(post("/api/mascotas/" + mascota1.getId() + "/consultas")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("Examen auxiliar inválido")));
        }

        @Test
        @DisplayName("Rejects consulta for non-existent pet (HTTP 404)")
        void testCreateConsultaPetNotFound() throws Exception {
            ConsultaCreateDTO req = ConsultaCreateDTO.builder()
                    .diagnostico("Diagnóstico")
                    .tratamiento("Tratamiento")
                    .build();

            mockMvc.perform(post("/api/mascotas/99999/consultas")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail", is("Mascota no encontrada")));
        }

        @Test
        @DisplayName("Cliente and Recepcionista cannot create consulta (HTTP 403)")
        void testCreateConsultaUnauthorizedRoles() throws Exception {
            ConsultaCreateDTO req = ConsultaCreateDTO.builder()
                    .diagnostico("Diagnóstico")
                    .tratamiento("Tratamiento")
                    .build();

            mockMvc.perform(post("/api/mascotas/" + mascota1.getId() + "/consultas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(post("/api/mascotas/" + mascota1.getId() + "/consultas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("2. Attending Scheduled Cita Tests")
    class AtenderCitaTests {

        private Cita citaProgramada;

        @BeforeEach
        void createCita() {
            citaProgramada = new Cita();
            citaProgramada.setFechaHora(LocalDateTime.now().plusDays(1));
            citaProgramada.setMotivo("Vacunación y chequeo anual");
            citaProgramada.setTipo("Consulta médica");
            citaProgramada.setEstado(EstadoCita.PROGRAMADA);
            citaProgramada.setMascota(mascota1);
            citaProgramada.setVeterinario(vetUser);
            citaProgramada = citaRepository.save(citaProgramada);

            // Optional linked OrdenExamen
            OrdenExamen ordenCita = new OrdenExamen();
            ordenCita.setCita(citaProgramada);
            ordenCita.setExamenAuxiliar(examenHemograma);
            ordenCita.setEstado("PENDIENTE");
            ordenCita.setFecha(LocalDateTime.now());
            ordenExamenRepository.save(ordenCita);
            citaProgramada.setOrdenExamen(ordenCita);
        }

        @Test
        @DisplayName("Attending cita marks it COMPLETADA, marks linked exam REALIZADO, and creates consulta")
        void testAtenderCitaSuccess() throws Exception {
            ConsultaCreateDTO req = ConsultaCreateDTO.builder()
                    .diagnostico("Paciente en excelente estado general")
                    .tratamiento("Continuar dieta habitual")
                    .peso(13.2)
                    .build();

            mockMvc.perform(post("/api/citas/" + citaProgramada.getId() + "/atender")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.cita_id", is(citaProgramada.getId().intValue())))
                    .andExpect(jsonPath("$.cita_motivo", is("Vacunación y chequeo anual")))
                    .andExpect(jsonPath("$.diagnostico", is("Paciente en excelente estado general")));

            // Verify cita state updated
            Cita updatedCita = citaRepository.findById(citaProgramada.getId()).orElseThrow();
            assertEquals(EstadoCita.COMPLETADA, updatedCita.getEstado());

            // Verify linked orden_examen transitioned to REALIZADO
            OrdenExamen updatedOrden = ordenExamenRepository.findById(citaProgramada.getOrdenExamen().getId()).orElseThrow();
            assertEquals("REALIZADO", updatedOrden.getEstado());
        }

        @Test
        @DisplayName("Rejects attending already attended appointment (HTTP 400)")
        void testAtenderCitaAlreadyAttended() throws Exception {
            ConsultaCreateDTO req = ConsultaCreateDTO.builder()
                    .diagnostico("Primera atención")
                    .tratamiento("Tratamiento")
                    .build();

            // First attend
            mockMvc.perform(post("/api/citas/" + citaProgramada.getId() + "/atender")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            // Second attend should fail
            mockMvc.perform(post("/api/citas/" + citaProgramada.getId() + "/atender")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", is("Esta cita ya fue atendida")));
        }

        @Test
        @DisplayName("Rejects attending cancelled appointment (HTTP 400)")
        void testAtenderCitaCancelled() throws Exception {
            citaProgramada.setEstado(EstadoCita.CANCELADA);
            citaProgramada.setMotivoCancelacion("Cliente no asistió");
            citaRepository.save(citaProgramada);

            ConsultaCreateDTO req = ConsultaCreateDTO.builder()
                    .diagnostico("Atención")
                    .tratamiento("Tratamiento")
                    .build();

            mockMvc.perform(post("/api/citas/" + citaProgramada.getId() + "/atender")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", is("No puedes atender una cita cancelada")));
        }
    }

    @Nested
    @DisplayName("3. Clinical History and Medical Order Queries Tests")
    class ClinicalHistoryTests {

        private Consulta consulta1;
        private RecetaMedica receta1;
        private OrdenExamen orden1;
        private ReferenciaMedica referencia1;

        @BeforeEach
        void createHistoryData() {
            consulta1 = new Consulta();
            consulta1.setMascota(mascota1);
            consulta1.setVeterinario(vetUser);
            consulta1.setFecha(LocalDateTime.now().minusDays(2));
            consulta1.setDiagnostico("Otitis externa bilateral");
            consulta1.setTratamiento("Gotas óticas c/12h por 7 días");
            consulta1.setPeso(12.0);
            consulta1 = consultaRepository.save(consulta1);

            receta1 = new RecetaMedica();
            receta1.setConsulta(consulta1);
            receta1.setNombreMedicamento("Otovet gotas");
            receta1.setDosis("4 gotas");
            receta1.setCantidad("1 frasco");
            receta1.setIndicaciones("Aplicar en ambos oídos");
            receta1.setFecha(LocalDateTime.now().minusDays(2));
            receta1 = recetaMedicaRepository.save(receta1);

            orden1 = new OrdenExamen();
            orden1.setConsulta(consulta1);
            orden1.setExamenAuxiliar(examenHemograma);
            orden1.setEstado("REALIZADO");
            orden1.setResultado("Cultivo de secreción ótica positivo a levaduras");
            orden1.setFecha(LocalDateTime.now().minusDays(2));
            orden1 = ordenExamenRepository.save(orden1);

            referencia1 = new ReferenciaMedica();
            referencia1.setConsulta(consulta1);
            referencia1.setCentroDestino("Clínica Dermatológica Vet");
            referencia1.setEspecialidad("Dermatología");
            referencia1.setMotivo("Otitis recurrente");
            referencia1.setEstado("ACTIVA");
            referencia1.setFecha(LocalDateTime.now().minusDays(2));
            referencia1 = referenciaMedicaRepository.save(referencia1);
        }

        @Test
        @DisplayName("Owner cliente can view pet consolidated clinical history")
        void testClientViewOwnPetHistorial() throws Exception {
            mockMvc.perform(get("/api/mascotas/" + mascota1.getId() + "/historial")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.consultas", hasSize(1)))
                    .andExpect(jsonPath("$.consultas[0].diagnostico", is("Otitis externa bilateral")))
                    .andExpect(jsonPath("$.recetas", hasSize(1)))
                    .andExpect(jsonPath("$.recetas[0].nombre_medicamento", is("Otovet gotas")))
                    .andExpect(jsonPath("$.examenes", hasSize(1)))
                    .andExpect(jsonPath("$.examenes[0].resultado", containsString("levaduras")))
                    .andExpect(jsonPath("$.referencias", hasSize(1)))
                    .andExpect(jsonPath("$.referencias[0].centro_destino", is("Clínica Dermatológica Vet")));
        }

        @Test
        @DisplayName("Cliente cannot view another client's pet clinical history (HTTP 403)")
        void testClientCannotViewOtherPetHistorial() throws Exception {
            mockMvc.perform(get("/api/mascotas/" + mascota1.getId() + "/historial")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser2)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail", is("No tienes permiso para ver esta mascota")));
        }

        @Test
        @DisplayName("Veterinario and Recepcionista can view pet clinical history")
        void testStaffViewPetHistorial() throws Exception {
            mockMvc.perform(get("/api/mascotas/" + mascota1.getId() + "/historial")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.consultas", hasSize(1)));
        }

        @Test
        @DisplayName("Individual order lists enforce ownership check")
        void testIndividualOrderEndpoints() throws Exception {
            // Recetas
            mockMvc.perform(get("/api/mascotas/" + mascota1.getId() + "/recetas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].nombre_medicamento", is("Otovet gotas")));

            mockMvc.perform(get("/api/mascotas/" + mascota1.getId() + "/recetas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser2)))
                    .andExpect(status().isForbidden());

            // Exámenes
            mockMvc.perform(get("/api/mascotas/" + mascota1.getId() + "/examenes")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mockMvc.perform(get("/api/mascotas/" + mascota1.getId() + "/examenes")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser2)))
                    .andExpect(status().isForbidden());

            // Referencias
            mockMvc.perform(get("/api/mascotas/" + mascota1.getId() + "/referencias")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            mockMvc.perform(get("/api/mascotas/" + mascota1.getId() + "/referencias")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser2)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("4. Lab Exam Result Updates Tests")
    class ExamResultUpdateTests {

        private OrdenExamen orden;

        @BeforeEach
        void createOrden() {
            orden = new OrdenExamen();
            orden.setExamenAuxiliar(examenHemograma);
            orden.setEstado("PENDIENTE");
            orden.setFecha(LocalDateTime.now());
            orden = ordenExamenRepository.save(orden);
        }

        @Test
        @DisplayName("Veterinario updates lab result, observations, and status")
        void testUpdateLabResultSuccess() throws Exception {
            OrdenExamenUpdateDTO req = OrdenExamenUpdateDTO.builder()
                    .resultado("Valores normales")
                    .estado("REALIZADO")
                    .observaciones("Ninguna alteración")
                    .build();

            mockMvc.perform(put("/api/examenes/" + orden.getId() + "/resultado")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultado", is("Valores normales")))
                    .andExpect(jsonPath("$.estado", is("REALIZADO")))
                    .andExpect(jsonPath("$.observaciones", is("Ninguna alteración")));
        }

        @Test
        @DisplayName("Providing resultado without estado defaults estado to REALIZADO")
        void testUpdateLabResultDefaultState() throws Exception {
            OrdenExamenUpdateDTO req = OrdenExamenUpdateDTO.builder()
                    .resultado("Leucocitos altos")
                    .build();

            mockMvc.perform(put("/api/examenes/" + orden.getId() + "/resultado")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado", is("REALIZADO")));
        }

        @Test
        @DisplayName("Rejects invalid estado (HTTP 400)")
        void testUpdateLabResultInvalidState() throws Exception {
            OrdenExamenUpdateDTO req = OrdenExamenUpdateDTO.builder()
                    .estado("ESTADO_INEXISTENTE")
                    .build();

            mockMvc.perform(put("/api/examenes/" + orden.getId() + "/resultado")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("Estado inválido")));
        }
    }

    @Nested
    @DisplayName("5. Customer Aggregated Medical Orders Tests")
    class MisOrdenesMedicasTests {

        @BeforeEach
        void setupOrders() {
            Consulta c = new Consulta();
            c.setMascota(mascota1);
            c.setVeterinario(vetUser);
            c.setDiagnostico("Control");
            c.setTratamiento("Continuar");
            c = consultaRepository.save(c);

            RecetaMedica r = new RecetaMedica();
            r.setConsulta(c);
            r.setNombreMedicamento("Antiparasitario");
            r.setFecha(LocalDateTime.now());
            recetaMedicaRepository.save(r);

            OrdenExamen o = new OrdenExamen();
            o.setConsulta(c);
            o.setExamenAuxiliar(examenHemograma);
            o.setEstado("REALIZADO");
            o.setResultado("Normal");
            o.setFecha(LocalDateTime.now());
            ordenExamenRepository.save(o);

            ReferenciaMedica ref = new ReferenciaMedica();
            ref.setConsulta(c);
            ref.setCentroDestino("Centro Especializado");
            ref.setMotivo("Consulta de seguimiento");
            ref.setFecha(LocalDateTime.now());
            referenciaMedicaRepository.save(ref);
        }

        @Test
        @DisplayName("Cliente fetches all medical orders aggregated across pets and enriched with mascota_nombre")
        void testMisOrdenesMedicas() throws Exception {
            mockMvc.perform(get("/api/ordenes-medicas/mias")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.recetas", hasSize(1)))
                    .andExpect(jsonPath("$.recetas[0].mascota_nombre", is("Bobi")))
                    .andExpect(jsonPath("$.examenes", hasSize(1)))
                    .andExpect(jsonPath("$.examenes[0].mascota_nombre", is("Bobi")))
                    .andExpect(jsonPath("$.referencias", hasSize(1)))
                    .andExpect(jsonPath("$.referencias[0].mascota_nombre", is("Bobi")));
        }

        @Test
        @DisplayName("Non-cliente cannot access /api/ordenes-medicas/mias (HTTP 403)")
        void testMisOrdenesMedicasNonClient() throws Exception {
            mockMvc.perform(get("/api/ordenes-medicas/mias")
                            .header("Authorization", "Bearer " + tokenFor(vetUser)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("6. Vacuna Creation & Stock Deduction Tests")
    class VacunaTests {

        @Test
        @DisplayName("Create vaccine with inventory link computes proxima_dosis and deducts stock")
        void testCreateVacunaWithProductStockDeduction() throws Exception {
            VacunaCreateDTO req = VacunaCreateDTO.builder()
                    .productoId(vacunaRabiaProd.getId())
                    .fechaAplicacion(LocalDate.now())
                    .intervaloDias(365)
                    .build();

            mockMvc.perform(post("/api/mascotas/" + mascota1.getId() + "/vacunas")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.tipo", is("Vacuna Antirrábica Nobivac")))
                    .andExpect(jsonPath("$.proxima_dosis", is(LocalDate.now().plusDays(365).toString())))
                    .andExpect(jsonPath("$.producto_id", is(vacunaRabiaProd.getId().intValue())))
                    .andExpect(jsonPath("$.producto_nombre", is("Vacuna Antirrábica Nobivac")));

            // Check stock deduction in Producto
            Producto p = productoRepository.findById(vacunaRabiaProd.getId()).orElseThrow();
            assertEquals(9, p.getStock());

            // Check stock deduction in StockAlmacen
            StockAlmacen sa = stockAlmacenRepository.findByProductoIdAndAlmacen(vacunaRabiaProd.getId(), "Disponible").orElseThrow();
            assertEquals(9, sa.getCantidad());
        }

        @Test
        @DisplayName("Rejects vaccine when inventory product has insufficient stock (HTTP 400)")
        void testCreateVacunaInsufficientStock() throws Exception {
            vacunaRabiaProd.setStock(0);
            productoRepository.save(vacunaRabiaProd);

            VacunaCreateDTO req = VacunaCreateDTO.builder()
                    .productoId(vacunaRabiaProd.getId())
                    .fechaAplicacion(LocalDate.now())
                    .build();

            mockMvc.perform(post("/api/mascotas/" + mascota1.getId() + "/vacunas")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("Stock insuficiente")));
        }

        @Test
        @DisplayName("Create vaccine without product requires explicit tipo")
        void testCreateVacunaManualTipo() throws Exception {
            VacunaCreateDTO req = VacunaCreateDTO.builder()
                    .tipo("Quíntuple canina")
                    .fechaAplicacion(LocalDate.now())
                    .proximaDosis(LocalDate.now().plusDays(21))
                    .build();

            mockMvc.perform(post("/api/mascotas/" + mascota1.getId() + "/vacunas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tipo", is("Quíntuple canina")))
                    .andExpect(jsonPath("$.proxima_dosis", is(LocalDate.now().plusDays(21).toString())));
        }
    }

    @Nested
    @DisplayName("7. 15-Day Vaccine Reminders Tests")
    class RecordatoriosTests {

        @BeforeEach
        void createVaccines() {
            // Vacuna due in 5 days (should be in reminders)
            Vacuna v1 = new Vacuna();
            v1.setTipo("Parvovirus booster");
            v1.setFechaAplicacion(LocalDate.now().minusDays(20));
            v1.setProximaDosis(LocalDate.now().plusDays(5));
            v1.setMascota(mascota1);
            v1.setVeterinario(vetUser);
            vacunaRepository.save(v1);

            // Vacuna due in 30 days (should NOT be in reminders)
            Vacuna v2 = new Vacuna();
            v2.setTipo("Antirrábica anual");
            v2.setFechaAplicacion(LocalDate.now().minusDays(335));
            v2.setProximaDosis(LocalDate.now().plusDays(30));
            v2.setMascota(mascota1);
            v2.setVeterinario(vetUser);
            vacunaRepository.save(v2);

            // Vacuna for mascota2 (cliente2) due in 3 days
            Vacuna v3 = new Vacuna();
            v3.setTipo("Triple felina");
            v3.setFechaAplicacion(LocalDate.now().minusDays(30));
            v3.setProximaDosis(LocalDate.now().plusDays(3));
            v3.setMascota(mascota2);
            v3.setVeterinario(vetUser);
            vacunaRepository.save(v3);
        }

        @Test
        @DisplayName("Staff sees all upcoming reminders due within 15 days sorted ascending")
        void testStaffRecordatorios() throws Exception {
            mockMvc.perform(get("/api/recordatorios")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].tipo", is("Triple felina"))) // plusDays(3) comes before plusDays(5)
                    .andExpect(jsonPath("$[1].tipo", is("Parvovirus booster")));
        }

        @Test
        @DisplayName("Cliente sees only reminders for their own pets")
        void testClienteRecordatoriosFiltered() throws Exception {
            mockMvc.perform(get("/api/recordatorios")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].tipo", is("Parvovirus booster")))
                    .andExpect(jsonPath("$[0].mascota_nombre", is("Bobi")))
                    .andExpect(jsonPath("$[0].cliente_nombre", is("Carlos Sanchez")));
        }
    }

    @Nested
    @DisplayName("8. Vacunas and Consultas embedded in Mascota and Cliente DTOs")
    class MascotaAndClienteEmbeddingTests {

        @BeforeEach
        void createMascotaDetails() {
            Consulta c = new Consulta();
            c.setMascota(mascota1);
            c.setVeterinario(vetUser);
            c.setDiagnostico("Diagnóstico previo");
            c.setTratamiento("Tratamiento previo");
            c.setFecha(LocalDateTime.now().minusDays(10));
            consultaRepository.save(c);

            Vacuna v = new Vacuna();
            v.setMascota(mascota1);
            v.setVeterinario(vetUser);
            v.setTipo("Antirrábica");
            v.setFechaAplicacion(LocalDate.now().minusMonths(6));
            vacunaRepository.save(v);
        }

        @Test
        @DisplayName("GET /api/mascotas includes vacunas and consultas in MascotaDTO")
        void testMascotaDTOIncludesVacunasAndConsultas() throws Exception {
            mockMvc.perform(get("/api/mascotas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.id == " + mascota1.getId() + ")].vacunas", notNullValue()))
                    .andExpect(jsonPath("$[?(@.id == " + mascota1.getId() + ")].consultas", notNullValue()));
        }

        @Test
        @DisplayName("GET /api/clientes includes vacunas and consultas in each MascotaDTO")
        void testClienteDTOIncludesVacunasAndConsultas() throws Exception {
            mockMvc.perform(get("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.id == " + cliente1.getId() + ")].mascotas[0].vacunas", notNullValue()))
                    .andExpect(jsonPath("$[?(@.id == " + cliente1.getId() + ")].mascotas[0].consultas", notNullValue()));
        }
    }
}
