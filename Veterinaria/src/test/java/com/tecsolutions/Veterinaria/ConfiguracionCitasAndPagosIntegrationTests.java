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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ConfiguracionCitasAndPagosIntegrationTests {

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
    private PagoRepository pagoRepository;

    @Autowired
    private ExamenAuxiliarRepository examenAuxiliarRepository;

    @Autowired
    private OrdenExamenRepository ordenExamenRepository;

    @Autowired
    private ConfiguracionRepository configuracionRepository;

    @Autowired
    private ComprobanteSeqRepository comprobanteSeqRepository;

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

    private Usuario adminUser;
    private Usuario vetUser;
    private Usuario recepUser;
    private Usuario clienteUser1;
    private Usuario clienteUser2;
    private Cliente cliente1;
    private Cliente cliente2;
    private Mascota mascota1;
    private Mascota mascota2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        rateLimitingFilter.reset();

        pagoRepository.deleteAll();
        ordenExamenRepository.deleteAll();
        citaRepository.deleteAll();
        examenAuxiliarRepository.deleteAll();
        configuracionRepository.deleteAll();
        comprobanteSeqRepository.deleteAll();
        mascotaRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
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

        // 3. Clients
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
    }

    private String tokenFor(Usuario usuario) {
        return jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().getValue());
    }

    @Nested
    @DisplayName("1. Clinic Operating Hours & Schedule Configuration Tests")
    class HorarioConfigTests {

        @Test
        @DisplayName("Authenticated users can read default schedule configuration")
        void testReadDefaultHorarios() throws Exception {
            mockMvc.perform(get("/api/configuracion/horarios")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.dias", hasItems(1, 2, 3, 4, 5, 6)))
                    .andExpect(jsonPath("$.hora_inicio", is("09:00")))
                    .andExpect(jsonPath("$.hora_fin", is("18:00")))
                    .andExpect(jsonPath("$.duracion_min", is(30)));
        }

        @Test
        @DisplayName("Staff can update schedule configuration; Cliente is forbidden (403)")
        void testUpdateHorariosPermissions() throws Exception {
            HorarioConfigDTO nuevoHorario = HorarioConfigDTO.builder()
                    .dias(List.of(1, 2, 3, 4, 5))
                    .horaInicio("08:00")
                    .horaFin("17:00")
                    .duracionMin(60)
                    .build();

            // Cliente cannot update
            mockMvc.perform(put("/api/configuracion/horarios")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nuevoHorario)))
                    .andExpect(status().isForbidden());

            // Admin can update
            mockMvc.perform(put("/api/configuracion/horarios")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(nuevoHorario)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.hora_inicio", is("08:00")))
                    .andExpect(jsonPath("$.hora_fin", is("17:00")))
                    .andExpect(jsonPath("$.duracion_min", is(60)));
        }

        @Test
        @DisplayName("Horario validation rejects invalid days, times, and durations (400)")
        void testHorarioValidations() throws Exception {
            // Invalid day (0 or 8)
            HorarioConfigDTO badDays = HorarioConfigDTO.builder()
                    .dias(List.of(0, 1, 2))
                    .horaInicio("09:00")
                    .horaFin("18:00")
                    .duracionMin(30)
                    .build();
            mockMvc.perform(put("/api/configuracion/horarios")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badDays)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("Día inválido")));

            // Start time after end time
            HorarioConfigDTO badTimes = HorarioConfigDTO.builder()
                    .dias(List.of(1, 2, 3))
                    .horaInicio("19:00")
                    .horaFin("18:00")
                    .duracionMin(30)
                    .build();
            mockMvc.perform(put("/api/configuracion/horarios")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badTimes)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("hora de inicio debe ser anterior")));

            // Invalid duration (not 15, 30, or 60)
            HorarioConfigDTO badDuration = HorarioConfigDTO.builder()
                    .dias(List.of(1, 2, 3))
                    .horaInicio("09:00")
                    .horaFin("18:00")
                    .duracionMin(45)
                    .build();
            mockMvc.perform(put("/api/configuracion/horarios")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badDuration)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("15, 30 o 60")));
        }
    }

    @Nested
    @DisplayName("2. Daily Slot Availability Calculation Tests")
    class SlotAvailabilityTests {

        @Test
        @DisplayName("Non-working day returns disponible = false and empty slots")
        void testAvailabilityNonWorkingDay() throws Exception {
            // Find a Sunday (isoweekday = 7) which is not in default [1..6]
            LocalDate sunday = LocalDate.now();
            while (sunday.getDayOfWeek().getValue() != 7) {
                sunday = sunday.plusDays(1);
            }

            mockMvc.perform(get("/api/citas/disponibilidad")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .param("fecha", sunday.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.disponible", is(false)))
                    .andExpect(jsonPath("$.horarios", hasSize(0)));
        }

        @Test
        @DisplayName("Working day returns slots, marking booked non-cancelled slot unavailable")
        void testAvailabilityWithBookedSlot() throws Exception {
            // Find a future Monday (isoweekday = 1)
            LocalDate monday = LocalDate.now().plusWeeks(1);
            while (monday.getDayOfWeek().getValue() != 1) {
                monday = monday.plusDays(1);
            }

            // Create an appointment at 10:00 on Monday
            Cita cita = new Cita();
            cita.setFechaHora(monday.atTime(10, 0));
            cita.setMascota(mascota1);
            cita.setVeterinario(vetUser);
            cita.setEstado(EstadoCita.PROGRAMADA);
            citaRepository.save(cita);

            // Availability query without veterinarian filter
            mockMvc.perform(get("/api/citas/disponibilidad")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .param("fecha", monday.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.disponible", is(true)))
                    .andExpect(jsonPath("$.horarios[?(@.hora == '10:00')].disponible", contains(false)))
                    .andExpect(jsonPath("$.horarios[?(@.hora == '10:30')].disponible", contains(true)));

            // Availability query filtered to another vet returns slot available
            mockMvc.perform(get("/api/citas/disponibilidad")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .param("fecha", monday.toString())
                            .param("veterinario_id", "99999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.disponible", is(true)))
                    .andExpect(jsonPath("$.horarios[?(@.hora == '10:00')].disponible", contains(true)));
        }

        @Test
        @DisplayName("Cancelled appointment does not mark the slot unavailable")
        void testAvailabilityCancelledSlotStaysAvailable() throws Exception {
            LocalDate monday = LocalDate.now().plusWeeks(2);
            while (monday.getDayOfWeek().getValue() != 1) {
                monday = monday.plusDays(1);
            }

            Cita cita = new Cita();
            cita.setFechaHora(monday.atTime(11, 0));
            cita.setMascota(mascota1);
            cita.setVeterinario(vetUser);
            cita.setEstado(EstadoCita.CANCELADA);
            cita.setMotivoCancelacion("No puede asistir");
            citaRepository.save(cita);

            mockMvc.perform(get("/api/citas/disponibilidad")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .param("fecha", monday.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.horarios[?(@.hora == '11:00')].disponible", contains(true)));
        }
    }

    @Nested
    @DisplayName("3. Payment Configuration Tests")
    class PagoConfigTests {

        @Test
        @DisplayName("Users can read payment config; Only Administrador can update it")
        void testPagoConfigCrudAndPermissions() throws Exception {
            // Read defaults
            mockMvc.perform(get("/api/configuracion/pago")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pago_requerido", is(true)))
                    .andExpect(jsonPath("$.costo_cita", is(30.0)))
                    .andExpect(jsonPath("$.metodos", hasItems("YAPE", "PLIN", "TARJETA", "EFECTIVO")));

            Map<String, Object> newConfig = Map.of(
                    "pago_requerido", false,
                    "costo_cita", 40.0,
                    "metodos", List.of("YAPE", "TARJETA")
            );

            // Vet cannot update (403)
            mockMvc.perform(put("/api/configuracion/pago")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newConfig)))
                    .andExpect(status().isForbidden());

            // Admin can update
            mockMvc.perform(put("/api/configuracion/pago")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newConfig)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pago_requerido", is(false)))
                    .andExpect(jsonPath("$.costo_cita", is(40.0)))
                    .andExpect(jsonPath("$.metodos", hasItems("YAPE", "TARJETA")));
        }
    }

    @Nested
    @DisplayName("4. Diagnostic Exam Catalog & Soft-Delete Tests")
    class ExamenAuxiliarCatalogTests {

        @Test
        @DisplayName("Admin creates exam, duplicate name rejected (400), catalog listed sorted")
        void testExamenAuxiliarCreateAndList() throws Exception {
            ExamenAuxiliarDTO ex1 = ExamenAuxiliarDTO.builder()
                    .nombre("Hemograma Completo")
                    .descripcion("Análisis sanguíneo")
                    .precio(45.0)
                    .tiempoEstimado("24 horas")
                    .activo(true)
                    .build();

            // Client cannot create (403)
            mockMvc.perform(post("/api/examenes-auxiliares")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ex1)))
                    .andExpect(status().isForbidden());

            // Admin creates
            mockMvc.perform(post("/api/examenes-auxiliares")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ex1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.nombre", is("Hemograma Completo")))
                    .andExpect(jsonPath("$.precio", is(45.0)));

            // Duplicate name rejected (400)
            mockMvc.perform(post("/api/examenes-auxiliares")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(ex1)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("Ya existe un examen")));

            // Listed
            mockMvc.perform(get("/api/examenes-auxiliares")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].nombre", is("Hemograma Completo")));
        }

        @Test
        @DisplayName("Soft-delete exam if referenced by OrdenExamen, otherwise hard delete")
        void testExamenAuxiliarDeleteBehavior() throws Exception {
            ExamenAuxiliar exReferenced = examenAuxiliarRepository.save(new ExamenAuxiliar(null, "Ecografía", "Ecografía abdominal", 80.0, "1 hora", true));
            ExamenAuxiliar exUnreferenced = examenAuxiliarRepository.save(new ExamenAuxiliar(null, "Rayos X", "Placa radiográfica", 60.0, "2 horas", true));

            // Create an OrdenExamen referencing exReferenced
            OrdenExamen orden = new OrdenExamen();
            orden.setExamenAuxiliar(exReferenced);
            orden.setEstado("PENDIENTE");
            orden.setFecha(LocalDateTime.now());
            ordenExamenRepository.save(orden);

            // Delete exUnreferenced -> hard deleted
            mockMvc.perform(delete("/api/examenes-auxiliares/" + exUnreferenced.getId())
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail", is("Examen eliminado")));
            assertTrue(examenAuxiliarRepository.findById(exUnreferenced.getId()).isEmpty());

            // Delete exReferenced -> soft deactivated
            mockMvc.perform(delete("/api/examenes-auxiliares/" + exReferenced.getId())
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail", containsString("Examen desactivado")));

            ExamenAuxiliar afterDelete = examenAuxiliarRepository.findById(exReferenced.getId()).orElse(null);
            assertNotNull(afterDelete);
            assertFalse(afterDelete.getActivo());
        }
    }

    @Nested
    @DisplayName("5. Appointment Booking & Conflict Detection Tests")
    class CitaBookingTests {

        @Test
        @DisplayName("Cliente books appointment for own pet with initial PENDIENTE status; other pet forbidden (403)")
        void testClientBookingAndOwnership() throws Exception {
            LocalDateTime fechaCita = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);

            // Cliente 1 attempts to book for Cliente 2's pet (mascota2) -> 403 Forbidden
            CitaDTO invalidBooking = CitaDTO.builder()
                    .mascotaId(mascota2.getId())
                    .fechaHora(fechaCita)
                    .motivo("Consulta general")
                    .build();

            mockMvc.perform(post("/api/citas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidBooking)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail", containsString("No tienes permiso")));

            // Cliente 1 books for own pet (mascota1) -> starts in PENDIENTE
            CitaDTO validBooking = CitaDTO.builder()
                    .mascotaId(mascota1.getId())
                    .fechaHora(fechaCita)
                    .motivo("Control de vacunas")
                    .tipo("Control / Seguimiento")
                    .build();

            mockMvc.perform(post("/api/citas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validBooking)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.estado", is("Pendiente")))
                    .andExpect(jsonPath("$.tipo", is("Control / Seguimiento")));
        }

        @Test
        @DisplayName("Staff booking starts in PROGRAMADA; Overlapping appointment returns 409 Conflict")
        void testStaffBookingAndConflictDetection() throws Exception {
            LocalDateTime fechaCita = LocalDateTime.now().plusDays(3).withHour(15).withMinute(0).withSecond(0).withNano(0);

            // Staff books appointment
            CitaDTO booking1 = CitaDTO.builder()
                    .mascotaId(mascota1.getId())
                    .veterinarioId(vetUser.getId())
                    .fechaHora(fechaCita)
                    .motivo("Chequeo general")
                    .build();

            mockMvc.perform(post("/api/citas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(booking1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado", is("Programada")))
                    .andExpect(jsonPath("$.veterinario_nombre", is("vet")));

            // Booking overlapping slot (same vet, 15 minutes later within 30 min duration) returns 409 Conflict
            CitaDTO conflictBooking = CitaDTO.builder()
                    .mascotaId(mascota2.getId())
                    .veterinarioId(vetUser.getId())
                    .fechaHora(fechaCita.plusMinutes(15))
                    .motivo("Emergencia")
                    .build();

            mockMvc.perform(post("/api/citas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(conflictBooking)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail", containsString("Ese horario ya está ocupado")));
        }

        @Test
        @DisplayName("Booking Examen auxiliar validates active exam and auto-creates OrdenExamen")
        void testExamenAuxiliarAutoCreatesOrdenExamen() throws Exception {
            ExamenAuxiliar exam = examenAuxiliarRepository.save(new ExamenAuxiliar(null, "Perfil Bioquímico", "Panel completo", 75.0, "48 horas", true));
            LocalDateTime fechaCita = LocalDateTime.now().plusDays(4).withHour(11).withMinute(0).withSecond(0).withNano(0);

            CitaDTO citaExam = CitaDTO.builder()
                    .mascotaId(mascota1.getId())
                    .fechaHora(fechaCita)
                    .tipo("Examen auxiliar")
                    .examenAuxiliarId(exam.getId())
                    .motivo("Sospecha renal")
                    .build();

            String responseBody = mockMvc.perform(post("/api/citas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(citaExam)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tipo", is("Examen auxiliar")))
                    .andExpect(jsonPath("$.examen_auxiliar_nombre", is("Perfil Bioquímico")))
                    .andReturn().getResponse().getContentAsString();

            JsonNode root = objectMapper.readTree(responseBody);
            Long citaId = root.get("id").asLong();

            // Verify OrdenExamen was created in DB
            List<OrdenExamen> ordenes = ordenExamenRepository.findAll();
            boolean created = ordenes.stream().anyMatch(o ->
                    o.getCita() != null && o.getCita().getId().equals(citaId) &&
                            o.getExamenAuxiliar().getId().equals(exam.getId()) &&
                            "PENDIENTE".equals(o.getEstado()) &&
                            "Sospecha renal".equals(o.getObservaciones()));
            assertTrue(created, "OrdenExamen should have been automatically created");
        }
    }

    @Nested
    @DisplayName("6. Rescheduling, Cancellation & Status Lifecycle Tests")
    class CitaLifecycleTests {

        @Test
        @DisplayName("Reschedule appointment succeeds, validates conflict (409) and cannot reschedule cancelled (400)")
        void testRescheduleLifecycle() throws Exception {
            LocalDateTime t1 = LocalDateTime.now().plusDays(5).withHour(9).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime t2 = t1.plusHours(1);

            Cita cita1 = citaRepository.save(new Cita(null, t1, "Consulta 1", "Consulta médica", EstadoCita.PROGRAMADA, null, mascota1, vetUser, null, null));
            Cita cita2 = citaRepository.save(new Cita(null, t2, "Consulta 2", "Consulta médica", EstadoCita.PROGRAMADA, null, mascota2, vetUser, null, null));

            // Reschedule cita1 into cita2's slot -> 409 Conflict
            ReprogramarCitaRequestDTO conflictReq = ReprogramarCitaRequestDTO.builder()
                    .fechaHora(t2)
                    .motivo("Mover hora")
                    .build();

            mockMvc.perform(put("/api/citas/" + cita1.getId() + "/reprogramar")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(conflictReq)))
                    .andExpect(status().isConflict());

            // Reschedule cita1 to empty slot -> 200 OK
            LocalDateTime t3 = t1.plusHours(2);
            ReprogramarCitaRequestDTO validReq = ReprogramarCitaRequestDTO.builder()
                    .fechaHora(t3)
                    .motivo("Nuevo horario libre")
                    .build();

            mockMvc.perform(put("/api/citas/" + cita1.getId() + "/reprogramar")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.motivo", is("Nuevo horario libre")));

            // Cancel cita1
            cita1.setEstado(EstadoCita.CANCELADA);
            citaRepository.save(cita1);

            // Rescheduling cancelled appointment fails (400)
            mockMvc.perform(put("/api/citas/" + cita1.getId() + "/reprogramar")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validReq)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("No puedes reprogramar una cita cancelada")));
        }

        @Test
        @DisplayName("Cliente cancels own appointment with motivo; Staff updates status requiring motivo for Cancelada")
        void testCancellationAndStatusUpdates() throws Exception {
            LocalDateTime t = LocalDateTime.now().plusDays(6).withHour(10).withMinute(0).withSecond(0).withNano(0);
            Cita cita = citaRepository.save(new Cita(null, t, "Consulta", "Consulta médica", EstadoCita.PROGRAMADA, null, mascota1, vetUser, null, null));

            // Cliente 2 cannot cancel Cliente 1's appointment (403)
            CancelarCitaRequestDTO cancelReq = CancelarCitaRequestDTO.builder()
                    .motivoCancelacion("Viaje urgente")
                    .build();

            mockMvc.perform(put("/api/citas/" + cita.getId() + "/cancelar")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser2))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(cancelReq)))
                    .andExpect(status().isForbidden());

            // Staff updates to Cancelada without motivo -> 400 Bad Request
            mockMvc.perform(put("/api/citas/" + cita.getId() + "/estado")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .param("estado", "Cancelada")
                            .param("motivo_cancelacion", "   "))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("Debe indicar el motivo")));

            // Staff updates to Cancelada with motivo -> 200 OK
            mockMvc.perform(put("/api/citas/" + cita.getId() + "/estado")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .param("estado", "Cancelada")
                            .param("motivo_cancelacion", "Emergencia médica del veterinario"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado", is("Cancelada")))
                    .andExpect(jsonPath("$.motivo_cancelacion", is("Emergencia médica del veterinario")));
        }

        @Test
        @DisplayName("GET /api/citas/mias returns enriched CitaConMascotaDTO for authenticated client only")
        void testListCitasMias() throws Exception {
            LocalDateTime t1 = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime t2 = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);

            citaRepository.save(new Cita(null, t1, "Cita de Bobi", "Consulta médica", EstadoCita.PROGRAMADA, null, mascota1, vetUser, null, null));
            citaRepository.save(new Cita(null, t2, "Cita de Michi", "Consulta médica", EstadoCita.PROGRAMADA, null, mascota2, vetUser, null, null));

            // Cliente 1 only sees mascota1's appointment
            mockMvc.perform(get("/api/citas/mias")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].mascota_nombre", is("Bobi")))
                    .andExpect(jsonPath("$[0].motivo", is("Cita de Bobi")));
        }

        @Test
        @DisplayName("DELETE /api/citas/{id} removes appointment for Admin/Vet, rejects Cliente (403)")
        void testDeleteCitaPermissions() throws Exception {
            LocalDateTime t = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0).withSecond(0).withNano(0);
            Cita cita = citaRepository.save(new Cita(null, t, "Cita a borrar", "Consulta médica", EstadoCita.PROGRAMADA, null, mascota1, null, null, null));

            // Cliente cannot delete (403)
            mockMvc.perform(delete("/api/citas/" + cita.getId())
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isForbidden());

            // Admin deletes
            mockMvc.perform(delete("/api/citas/" + cita.getId())
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail", is("Cita eliminada")));

            assertTrue(citaRepository.findById(cita.getId()).isEmpty());
        }
    }

    @Nested
    @DisplayName("7. Pago Creation, Processing & Electronic Receipt Generation Tests")
    class PagoAndBillingTests {

        @Test
        @DisplayName("Pago creation calculates tariff when monto is omitted; validates payment method (400)")
        void testPagoTariffCalculationAndValidation() throws Exception {
            LocalDateTime t = LocalDateTime.now().plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0);
            Cita cita = citaRepository.save(new Cita(null, t, "Vacunación anual", "Vacunación", EstadoCita.PENDIENTE, null, mascota1, null, null, null));

            // Unsupported method -> 400
            PagoCreateDTO badMethod = PagoCreateDTO.builder()
                    .metodo("BITCOIN")
                    .build();

            mockMvc.perform(post("/api/citas/" + cita.getId() + "/pagos")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badMethod)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail", containsString("no soportado")));

            // Valid payment without monto: uses Vacunación tariff (35.0)
            PagoCreateDTO validPago = PagoCreateDTO.builder()
                    .metodo("YAPE")
                    .tipoComprobante("Boleta")
                    .build();

            mockMvc.perform(post("/api/citas/" + cita.getId() + "/pagos")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPago)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.monto", is(35.0)))
                    .andExpect(jsonPath("$.estado", is("PENDIENTE")))
                    .andExpect(jsonPath("$.referencia", notNullValue()));
        }

        @Test
        @DisplayName("Processing payment transitions Cita to PROGRAMADA and generates correlative BA64 / FA64 receipt")
        void testProcessPaymentAndReceiptGeneration() throws Exception {
            LocalDateTime t = LocalDateTime.now().plusDays(4).withHour(10).withMinute(0).withSecond(0).withNano(0);
            Cita cita = citaRepository.save(new Cita(null, t, "Consulta", "Consulta médica", EstadoCita.PENDIENTE, null, mascota1, null, null, null));

            // 1. Create Pago 1 (Boleta)
            PagoCreateDTO pagoReq1 = PagoCreateDTO.builder()
                    .metodo("PLIN")
                    .tipoComprobante("Boleta")
                    .build();

            String p1Str = mockMvc.perform(post("/api/citas/" + cita.getId() + "/pagos")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pagoReq1)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Long pagoId1 = objectMapper.readTree(p1Str).get("id").asLong();

            // 2. Process Pago 1 successfully
            mockMvc.perform(post("/api/pagos/" + pagoId1 + "/procesar")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("success", true))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado", is("COMPLETADO")))
                    .andExpect(jsonPath("$.serie", is("BA64")))
                    .andExpect(jsonPath("$.numero", is("0000000001")))
                    .andExpect(jsonPath("$.comprobante", is("BA64-0000000001")));

            // Cita should now be transitioned to PROGRAMADA
            Cita updatedCita = citaRepository.findById(cita.getId()).orElse(null);
            assertNotNull(updatedCita);
            assertEquals(EstadoCita.PROGRAMADA, updatedCita.getEstado());

            // 3. Create Pago 2 with Factura and valid 11-digit RUC
            Cita cita2 = citaRepository.save(new Cita(null, t.plusHours(1), "Cirugía", "Cirugía", EstadoCita.PENDIENTE, null, mascota1, null, null, null));

            PagoCreateDTO pagoReq2 = PagoCreateDTO.builder()
                    .metodo("TARJETA")
                    .tipoComprobante("Factura")
                    .documentoFiscal("20123456789")
                    .build();

            String p2Str = mockMvc.perform(post("/api/citas/" + cita2.getId() + "/pagos")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pagoReq2)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Long pagoId2 = objectMapper.readTree(p2Str).get("id").asLong();

            mockMvc.perform(post("/api/pagos/" + pagoId2 + "/procesar")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("success", true))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado", is("COMPLETADO")))
                    .andExpect(jsonPath("$.serie", is("FA64")))
                    .andExpect(jsonPath("$.numero", is("0000000001")))
                    .andExpect(jsonPath("$.comprobante", is("FA64-0000000001")));

            // 4. Client lists their payments
            mockMvc.perform(get("/api/pagos/mios")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].comprobante", hasItems("BA64-0000000001", "FA64-0000000001")));
        }

        @Test
        @DisplayName("Factura with invalid RUC falls back to Boleta; Failed payment marks FALLIDO")
        void testFacturaFallbackAndFailedPayment() throws Exception {
            LocalDateTime t = LocalDateTime.now().plusDays(5).withHour(11).withMinute(0).withSecond(0).withNano(0);
            Cita cita = citaRepository.save(new Cita(null, t, "Consulta", "Consulta médica", EstadoCita.PENDIENTE, null, mascota1, null, null, null));

            // Factura with 8-digit DNI -> falls back to Boleta BA64
            PagoCreateDTO pagoReq = PagoCreateDTO.builder()
                    .metodo("EFECTIVO")
                    .tipoComprobante("Factura")
                    .documentoFiscal("12345678") // only 8 digits, not 11
                    .build();

            String pStr = mockMvc.perform(post("/api/citas/" + cita.getId() + "/pagos")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pagoReq)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Long pagoId = objectMapper.readTree(pStr).get("id").asLong();

            mockMvc.perform(post("/api/pagos/" + pagoId + "/procesar")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("success", true))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tipo_comprobante", is("Boleta")))
                    .andExpect(jsonPath("$.serie", is("BA64")));

            // Failed payment simulation
            Cita citaFail = citaRepository.save(new Cita(null, t.plusHours(2), "Consulta", "Consulta médica", EstadoCita.PENDIENTE, null, mascota1, null, null, null));
            PagoCreateDTO pagoFailReq = PagoCreateDTO.builder()
                    .metodo("YAPE")
                    .build();

            String pFailStr = mockMvc.perform(post("/api/citas/" + citaFail.getId() + "/pagos")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(pagoFailReq)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Long pagoFailId = objectMapper.readTree(pFailStr).get("id").asLong();

            mockMvc.perform(post("/api/pagos/" + pagoFailId + "/procesar")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("success", false))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.estado", is("FALLIDO")));

            // Cita remains PENDIENTE
            Cita unchangedCita = citaRepository.findById(citaFail.getId()).orElse(null);
            assertNotNull(unchangedCita);
            assertEquals(EstadoCita.PENDIENTE, unchangedCita.getEstado());
        }
    }
}
