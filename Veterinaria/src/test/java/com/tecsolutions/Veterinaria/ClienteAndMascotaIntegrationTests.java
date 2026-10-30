package com.tecsolutions.Veterinaria;

import com.tecsolutions.Veterinaria.dto.ClienteDTO;
import com.tecsolutions.Veterinaria.dto.FotoRequestDTO;
import com.tecsolutions.Veterinaria.dto.MascotaDTO;
import com.tecsolutions.Veterinaria.model.Cliente;
import com.tecsolutions.Veterinaria.model.Mascota;
import com.tecsolutions.Veterinaria.model.RolEnum;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.repository.AuditoriaRepository;
import com.tecsolutions.Veterinaria.repository.ClienteRepository;
import com.tecsolutions.Veterinaria.repository.MascotaRepository;
import com.tecsolutions.Veterinaria.repository.PasswordResetTokenRepository;
import com.tecsolutions.Veterinaria.repository.UsuarioRepository;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ClienteAndMascotaIntegrationTests {

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
    private PasswordResetTokenRepository passwordResetTokenRepository;

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
    @DisplayName("1. Staff Cliente Listing & Creation Tests")
    class StaffClientManagementTests {

        @Test
        @DisplayName("Staff (Admin, Vet, Recep) can list clients with pagination and nested pets")
        void testListClientsStaffSuccess() throws Exception {
            // Admin lists
            mockMvc.perform(get("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .param("skip", "0")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].dni", anyOf(is("11111111"), is("22222222"))))
                    .andExpect(jsonPath("$[0].mascotas", notNullValue()));

            // Vet lists
            mockMvc.perform(get("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(vetUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));

            // Recep lists
            mockMvc.perform(get("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Cliente role is rejected with 403 when listing all clients")
        void testListClientsClienteForbidden() throws Exception {
            mockMvc.perform(get("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("No tienes permisos suficientes para realizar esta acción"));
        }

        @Test
        @DisplayName("Staff creates client without portal account (no username/password)")
        void testCreateClientWithoutPortalAccount() throws Exception {
            String json = """
                    {
                        "dni": "33333333",
                        "nombre": "Pedro Ramirez",
                        "telefono": "933333333",
                        "email": "pedro@sirbull.pe"
                    }
                    """;

            mockMvc.perform(post("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.dni").value("33333333"))
                    .andExpect(jsonPath("$.nombre").value("Pedro Ramirez"))
                    .andExpect(jsonPath("$.usuario_id").doesNotExist());

            assertTrue(clienteRepository.findByDni("33333333").isPresent());
        }

        @Test
        @DisplayName("Staff creates client with portal account (username and password)")
        void testCreateClientWithPortalAccount() throws Exception {
            String json = """
                    {
                        "dni": "44444444",
                        "nombre": "Ana Flores",
                        "telefono": "944444444",
                        "email": "ana@sirbull.pe",
                        "username": "anaflores",
                        "password": "SecretPassword123"
                    }
                    """;

            mockMvc.perform(post("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.dni").value("44444444"))
                    .andExpect(jsonPath("$.usuario_id").isNumber())
                    .andExpect(jsonPath("$.username").doesNotExist())
                    .andExpect(jsonPath("$.password").doesNotExist());

            Usuario nuevoUsuario = usuarioRepository.findByUsername("anaflores").orElseThrow();
            assertEquals(RolEnum.CLIENTE, nuevoUsuario.getRol());
            assertTrue(passwordEncoder.matches("SecretPassword123", nuevoUsuario.getHashedPassword()));
        }

        @Test
        @DisplayName("Creation fails with 400 when only username is provided")
        void testCreateClientOnlyUsernameError() throws Exception {
            String json = """
                    {
                        "dni": "55555555",
                        "nombre": "Incomplete User",
                        "telefono": "955555555",
                        "email": "incomplete@sirbull.pe",
                        "username": "incomplete"
                    }
                    """;

            mockMvc.perform(post("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Debe indicar usuario y contraseña para crear la cuenta de acceso"));
        }

        @Test
        @DisplayName("Creation fails with 400 when only password is provided")
        void testCreateClientOnlyPasswordError() throws Exception {
            String json = """
                    {
                        "dni": "55555555",
                        "nombre": "Incomplete User",
                        "telefono": "955555555",
                        "email": "incomplete@sirbull.pe",
                        "password": "PasswordOnly123"
                    }
                    """;

            mockMvc.perform(post("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Debe indicar usuario y contraseña para crear la cuenta de acceso"));
        }

        @Test
        @DisplayName("Creation fails with 400 when username already exists")
        void testCreateClientDuplicateUsernameError() throws Exception {
            String json = """
                    {
                        "dni": "55555555",
                        "nombre": "Dup User",
                        "telefono": "955555555",
                        "email": "dup@sirbull.pe",
                        "username": "cliente1",
                        "password": "ValidPassword123"
                    }
                    """;

            mockMvc.perform(post("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("El usuario ya existe"));
        }

        @Test
        @DisplayName("Creation fails with 400 when DNI or phone already exists")
        void testCreateClientDuplicateDniOrTelefonoError() throws Exception {
            // Duplicate DNI
            String jsonDni = """
                    {
                        "dni": "11111111",
                        "nombre": "Same DNI",
                        "telefono": "999999999",
                        "email": "samedni@sirbull.pe"
                    }
                    """;

            mockMvc.perform(post("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonDni))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("El DNI o Teléfono ya está registrado"));

            // Duplicate Phone
            String jsonTel = """
                    {
                        "dni": "66666666",
                        "nombre": "Same Tel",
                        "telefono": "911111111",
                        "email": "sametel@sirbull.pe"
                    }
                    """;

            mockMvc.perform(post("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTel))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("El DNI o Teléfono ya está registrado"));
        }

        @Test
        @DisplayName("Cliente role cannot create clients (403 Forbidden)")
        void testCreateClientClienteForbidden() throws Exception {
            String json = """
                    {
                        "dni": "77777777",
                        "nombre": "Unauthorized Client Creation",
                        "telefono": "977777777"
                    }
                    """;

            mockMvc.perform(post("/api/clientes")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("No tienes permisos suficientes para realizar esta acción"));
        }
    }

    @Nested
    @DisplayName("2. Client Portal Profile & Photo Tests")
    class ClientProfileAndPhotoTests {

        @Test
        @DisplayName("Authenticated client retrieves own profile with nested pets via GET /api/clientes/me")
        void testGetClientProfileMeSuccess() throws Exception {
            mockMvc.perform(get("/api/clientes/me")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(cliente1.getId()))
                    .andExpect(jsonPath("$.dni").value("11111111"))
                    .andExpect(jsonPath("$.nombre").value("Carlos Sanchez"))
                    .andExpect(jsonPath("$.mascotas", hasSize(1)))
                    .andExpect(jsonPath("$.mascotas[0].nombre").value("Bobi"))
                    .andExpect(jsonPath("$.mascotas[0].especie").value("Canino"));
        }

        @Test
        @DisplayName("Authenticated client without profile record gets 404")
        void testGetClientProfileMeNotFound() throws Exception {
            Usuario orphanCliente = usuarioRepository.save(new Usuario(null, "orphan", passwordEncoder.encode("pass"), RolEnum.CLIENTE, true, "orphan@sirbull.pe"));

            mockMvc.perform(get("/api/clientes/me")
                            .header("Authorization", "Bearer " + tokenFor(orphanCliente)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("No existe un perfil de cliente asociado a este usuario"));
        }

        @Test
        @DisplayName("Staff (Admin, Vet, Recep) cannot access /api/clientes/me (403 Forbidden)")
        void testGetClientProfileMeForbiddenForStaff() throws Exception {
            mockMvc.perform(get("/api/clientes/me")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("No tienes permisos suficientes para realizar esta acción"));
        }

        @Test
        @DisplayName("Authenticated client updates photo via PUT /api/clientes/me/foto")
        void testUpdateClientPhotoSuccess() throws Exception {
            String fotoPayload = """
                    {
                        "foto_base64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA"
                    }
                    """;

            mockMvc.perform(put("/api/clientes/me/foto")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(fotoPayload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.foto_url").value("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA"));

            Cliente updated = clienteRepository.findById(cliente1.getId()).orElseThrow();
            assertEquals("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA", updated.getFotoUrl());
        }
    }

    @Nested
    @DisplayName("3. Admin Client Deletion & Cascading Tests")
    class AdminDeleteClientTests {

        @Test
        @DisplayName("Admin deletes client and cascades deletion to pets")
        void testDeleteClientSuccessCascadingPets() throws Exception {
            mockMvc.perform(delete("/api/clientes/" + cliente1.getId())
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail").value("Cliente eliminado"));

            assertFalse(clienteRepository.findById(cliente1.getId()).isPresent());
            assertFalse(mascotaRepository.findById(mascota1.getId()).isPresent());
            // cliente2 and mascota2 should still exist
            assertTrue(clienteRepository.findById(cliente2.getId()).isPresent());
            assertTrue(mascotaRepository.findById(mascota2.getId()).isPresent());
        }

        @Test
        @DisplayName("Non-admin roles cannot delete clients (403 Forbidden)")
        void testDeleteClientNonAdminForbidden() throws Exception {
            mockMvc.perform(delete("/api/clientes/" + cliente1.getId())
                            .header("Authorization", "Bearer " + tokenFor(vetUser)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(delete("/api/clientes/" + cliente1.getId())
                            .header("Authorization", "Bearer " + tokenFor(recepUser)))
                    .andExpect(status().isForbidden());

            mockMvc.perform(delete("/api/clientes/" + cliente1.getId())
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Admin receives 404 when deleting nonexistent client")
        void testDeleteClientNotFound() throws Exception {
            mockMvc.perform(delete("/api/clientes/999999")
                            .header("Authorization", "Bearer " + tokenFor(adminUser)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Cliente no encontrado"));
        }
    }

    @Nested
    @DisplayName("4. Mascota Creation Tests")
    class PetCreationTests {

        @Test
        @DisplayName("Staff creates pet under client via POST /api/clientes/{cliente_id}/mascotas")
        void testCreatePetForClientStaffSuccess() throws Exception {
            String json = """
                    {
                        "nombre": "Rocky",
                        "especie": "Canino",
                        "raza": "Bulldog",
                        "fecha_nacimiento": "2023-01-20",
                        "foto_url": "http://img.sirbull.pe/rocky.png",
                        "activo": true
                    }
                    """;

            mockMvc.perform(post("/api/clientes/" + cliente1.getId() + "/mascotas")
                            .header("Authorization", "Bearer " + tokenFor(recepUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.nombre").value("Rocky"))
                    .andExpect(jsonPath("$.especie").value("Canino"))
                    .andExpect(jsonPath("$.raza").value("Bulldog"))
                    .andExpect(jsonPath("$.cliente_id").value(cliente1.getId()))
                    .andExpect(jsonPath("$.activo").value(true))
                    .andExpect(jsonPath("$.vacunas", hasSize(0)))
                    .andExpect(jsonPath("$.consultas", hasSize(0)));

            List<Mascota> pets = mascotaRepository.findByPropietarioId(cliente1.getId());
            assertEquals(2, pets.size());
        }

        @Test
        @DisplayName("Creating pet fails with 404 when client does not exist")
        void testCreatePetClientNotFound() throws Exception {
            String json = """
                    {
                        "nombre": "Ghost",
                        "especie": "Canino"
                    }
                    """;

            mockMvc.perform(post("/api/clientes/999999/mascotas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Cliente no encontrado"));
        }

        @Test
        @DisplayName("Cliente role cannot register pets via staff endpoint (403 Forbidden)")
        void testCreatePetClienteForbidden() throws Exception {
            String json = """
                    {
                        "nombre": "Ghost",
                        "especie": "Canino"
                    }
                    """;

            mockMvc.perform(post("/api/clientes/" + cliente1.getId() + "/mascotas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("No tienes permisos suficientes para realizar esta acción"));
        }
    }

    @Nested
    @DisplayName("5. Mascota Listing & Ownership Isolation Tests")
    class PetListingAndOwnershipTests {

        @Test
        @DisplayName("Staff sees all pets across all clients")
        void testListPetsStaffSeesAll() throws Exception {
            mockMvc.perform(get("/api/mascotas")
                            .header("Authorization", "Bearer " + tokenFor(vetUser)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].nombre", containsInAnyOrder("Bobi", "Michi")));
        }

        @Test
        @DisplayName("Cliente user sees only their own pets (ownership isolation)")
        void testListPetsClienteSeesOnlyOwn() throws Exception {
            // clienteUser1 owns Bobi only
            mockMvc.perform(get("/api/mascotas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].nombre").value("Bobi"))
                    .andExpect(jsonPath("$[0].cliente_id").value(cliente1.getId()));

            // clienteUser2 owns Michi only
            mockMvc.perform(get("/api/mascotas")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser2)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].nombre").value("Michi"))
                    .andExpect(jsonPath("$[0].cliente_id").value(cliente2.getId()));
        }

        @Test
        @DisplayName("Listing pets respects pagination parameters skip and limit")
        void testListPetsPagination() throws Exception {
            mockMvc.perform(get("/api/mascotas")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .param("skip", "1")
                            .param("limit", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("6. Mascota Photo Update & Ownership Enforcement Tests")
    class PetPhotoUpdateTests {

        @Test
        @DisplayName("Client can update photo for their own pet")
        void testUpdatePetPhotoOwnerSuccess() throws Exception {
            String json = """
                    {
                        "foto_base64": "data:image/png;base64,newBobiPhotoBase64"
                    }
                    """;

            mockMvc.perform(put("/api/mascotas/" + mascota1.getId() + "/foto")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser1))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(mascota1.getId()))
                    .andExpect(jsonPath("$.foto_url").value("data:image/png;base64,newBobiPhotoBase64"));

            Mascota updated = mascotaRepository.findById(mascota1.getId()).orElseThrow();
            assertEquals("data:image/png;base64,newBobiPhotoBase64", updated.getFotoUrl());
        }

        @Test
        @DisplayName("Client gets 403 Forbidden when attempting to update another client's pet photo")
        void testUpdatePetPhotoOtherClientForbidden() throws Exception {
            String json = """
                    {
                        "foto_base64": "data:image/png;base64,maliciousPhoto"
                    }
                    """;

            // clienteUser2 attempts to update mascota1 (owned by cliente1)
            mockMvc.perform(put("/api/mascotas/" + mascota1.getId() + "/foto")
                            .header("Authorization", "Bearer " + tokenFor(clienteUser2))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("No puedes modificar una mascota que no te pertenece"));
        }

        @Test
        @DisplayName("Staff can update photo for any pet")
        void testUpdatePetPhotoStaffAllowed() throws Exception {
            String json = """
                    {
                        "foto_base64": "data:image/png;base64,vetUploadedPhoto"
                    }
                    """;

            mockMvc.perform(put("/api/mascotas/" + mascota2.getId() + "/foto")
                            .header("Authorization", "Bearer " + tokenFor(vetUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(mascota2.getId()))
                    .andExpect(jsonPath("$.foto_url").value("data:image/png;base64,vetUploadedPhoto"));
        }

        @Test
        @DisplayName("Updating photo for nonexistent pet returns 404 Not Found")
        void testUpdatePetPhotoNotFound() throws Exception {
            String json = """
                    {
                        "foto_base64": "data:image/png;base64,somePhoto"
                    }
                    """;

            mockMvc.perform(put("/api/mascotas/999999/foto")
                            .header("Authorization", "Bearer " + tokenFor(adminUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.detail").value("Mascota no encontrada"));
        }
    }
}
