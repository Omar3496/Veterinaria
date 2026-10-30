package com.tecsolutions.Veterinaria;

import com.tecsolutions.Veterinaria.dto.RecuperarPasswordRequestDTO;
import com.tecsolutions.Veterinaria.dto.RegistroClienteRequestDTO;
import com.tecsolutions.Veterinaria.dto.ResetPasswordRequestDTO;
import com.tecsolutions.Veterinaria.dto.UsuarioCreateDTO;
import com.tecsolutions.Veterinaria.dto.UsuarioUpdateDTO;
import com.tecsolutions.Veterinaria.model.Auditoria;
import com.tecsolutions.Veterinaria.model.Cliente;
import com.tecsolutions.Veterinaria.model.PasswordResetToken;
import com.tecsolutions.Veterinaria.model.RolEnum;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.repository.AuditoriaRepository;
import com.tecsolutions.Veterinaria.repository.ClienteRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class AuthAndSecurityIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

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
    private Usuario clienteUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        rateLimitingFilter.reset();
        passwordResetTokenRepository.deleteAll();
        auditoriaRepository.deleteAll();
        clienteRepository.deleteAll();
        usuarioRepository.deleteAll();

        adminUser = new Usuario(null, "admin", passwordEncoder.encode("admin123"), RolEnum.ADMIN, true, "admin@sirbull.pe");
        adminUser = usuarioRepository.save(adminUser);

        vetUser = new Usuario(null, "vet", passwordEncoder.encode("vet123"), RolEnum.VETERINARIO, true, "vet@sirbull.pe");
        vetUser = usuarioRepository.save(vetUser);

        recepUser = new Usuario(null, "recep", passwordEncoder.encode("recep123"), RolEnum.RECEPCIONISTA, true, "recep@sirbull.pe");
        recepUser = usuarioRepository.save(recepUser);

        clienteUser = new Usuario(null, "cliente", passwordEncoder.encode("cliente123"), RolEnum.CLIENTE, true, "cliente@sirbull.pe");
        clienteUser = usuarioRepository.save(clienteUser);

        Cliente c = new Cliente();
        c.setDni("87654321");
        c.setNombre("Cliente Juan");
        c.setTelefono("987654321");
        c.setEmail("cliente@sirbull.pe");
        c.setUsuario(clienteUser);
        clienteRepository.save(c);
    }

    private String tokenFor(Usuario usuario) {
        return jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().getValue());
    }

    @Nested
    @DisplayName("1. POST /token Authentication & JWT Tests")
    class TokenTests {

        @Test
        @DisplayName("Authenticates successfully with username and password (form-encoded)")
        void loginWithUsernameSuccess() throws Exception {
            mockMvc.perform(post("/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "admin")
                            .param("password", "admin123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token").isString())
                    .andExpect(jsonPath("$.token_type").value("bearer"));

            List<Auditoria> logs = auditoriaRepository.findAll();
            assertFalse(logs.isEmpty());
            Auditoria loginLog = logs.getFirst();
            assertEquals("LOGIN", loginLog.getAccion());
            assertEquals("/token", loginLog.getEndpoint());
            assertEquals(adminUser.getId(), loginLog.getUsuario().getId());
        }

        @Test
        @DisplayName("Authenticates successfully with email and password")
        void loginWithEmailSuccess() throws Exception {
            mockMvc.perform(post("/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "admin@sirbull.pe")
                            .param("password", "admin123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token").isString())
                    .andExpect(jsonPath("$.token_type").value("bearer"));
        }

        @Test
        @DisplayName("Authenticates successfully with linked cliente email")
        void loginWithClienteEmailSuccess() throws Exception {
            mockMvc.perform(post("/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "cliente@sirbull.pe")
                            .param("password", "cliente123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token").isString());
        }

        @Test
        @DisplayName("Rejects wrong password with 401 and exact detail message")
        void loginBadPasswordFails() throws Exception {
            mockMvc.perform(post("/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "admin")
                            .param("password", "wrongpass"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Incorrect username or password"));
        }

        @Test
        @DisplayName("Rejects nonexistent user with 401")
        void loginNonexistentUserFails() throws Exception {
            mockMvc.perform(post("/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "nobody")
                            .param("password", "password"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Incorrect username or password"));
        }

        @Test
        @DisplayName("Rejects inactive user with 401")
        void loginInactiveUserFails() throws Exception {
            adminUser.setIsActive(false);
            usuarioRepository.save(adminUser);

            mockMvc.perform(post("/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "admin")
                            .param("password", "admin123"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Incorrect username or password"));
        }

        @Test
        @DisplayName("Rate limiting: allows 5 attempts, rejects 6th with HTTP 429")
        void rateLimitingEnforced() throws Exception {
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/token")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", "admin")
                                .param("password", "wrongpassword")
                                .header("X-Forwarded-For", "192.168.1.50"))
                        .andExpect(status().isUnauthorized());
            }

            mockMvc.perform(post("/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "admin")
                            .param("password", "wrongpassword")
                            .header("X-Forwarded-For", "192.168.1.50"))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.detail").value(containsString("Demasiados intentos")));
        }
    }

    @Nested
    @DisplayName("2. Security Headers & CORS Tests")
    class SecurityHeadersTests {

        @Test
        @DisplayName("Includes OWASP security headers on responses")
        void securityHeadersPresent() throws Exception {
            mockMvc.perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Strict-Transport-Security", containsString("max-age=63072000")))
                    .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                    .andExpect(header().exists("X-Frame-Options"))
                    .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")));
        }
    }

    @Nested
    @DisplayName("3. /api/usuarios CRUD & RBAC Tests")
    class UsuarioCrudTests {

        @Test
        @DisplayName("Admin can list users; password and hashed_password are not leaked")
        void adminCanListUsuarios() throws Exception {
            String token = tokenFor(adminUser);

            mockMvc.perform(get("/api/usuarios")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(4)))
                    .andExpect(jsonPath("$[0].username").exists())
                    .andExpect(jsonPath("$[0].rol").exists())
                    .andExpect(jsonPath("$[0].is_active").exists())
                    .andExpect(jsonPath("$[0].hashed_password").doesNotExist())
                    .andExpect(jsonPath("$[0].password").doesNotExist());
        }

        @Test
        @DisplayName("Non-admin roles are forbidden (403) from /api/usuarios")
        void nonAdminForbiddenFromUsuarios() throws Exception {
            for (Usuario u : List.of(vetUser, recepUser, clienteUser)) {
                String token = tokenFor(u);
                mockMvc.perform(get("/api/usuarios")
                                .header("Authorization", "Bearer " + token))
                        .andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.detail").value("No tienes permisos suficientes para realizar esta acción"));
            }
        }

        @Test
        @DisplayName("Unauthenticated request to /api/usuarios returns 401")
        void unauthenticatedReturns401() throws Exception {
            mockMvc.perform(get("/api/usuarios"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.detail").value("Could not validate credentials"));
        }

        @Test
        @DisplayName("Admin creates a new user successfully")
        void adminCreatesUsuarioSuccess() throws Exception {
            String token = tokenFor(adminUser);
            UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                    .username("newvet")
                    .password("secret123")
                    .rol("Veterinario")
                    .email("newvet@sirbull.pe")
                    .build();

            mockMvc.perform(post("/api/usuarios")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.username").value("newvet"))
                    .andExpect(jsonPath("$.rol").value("Veterinario"))
                    .andExpect(jsonPath("$.is_active").value(true))
                    .andExpect(jsonPath("$.email").value("newvet@sirbull.pe"))
                    .andExpect(jsonPath("$.password").doesNotExist())
                    .andExpect(jsonPath("$.hashed_password").doesNotExist());

            Optional<Usuario> created = usuarioRepository.findByUsername("newvet");
            assertTrue(created.isPresent());
            assertTrue(passwordEncoder.matches("secret123", created.get().getHashedPassword()));
        }

        @Test
        @DisplayName("Creating user with duplicate username returns 400")
        void adminCreatesDuplicateUsernameFails() throws Exception {
            String token = tokenFor(adminUser);
            UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                    .username("admin")
                    .password("secret123")
                    .rol("Veterinario")
                    .build();

            mockMvc.perform(post("/api/usuarios")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("El nombre de usuario ya existe"));
        }

        @Test
        @DisplayName("Creating user with invalid role returns 400")
        void adminCreatesInvalidRoleFails() throws Exception {
            String token = tokenFor(adminUser);
            UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                    .username("badroleuser")
                    .password("secret123")
                    .rol("SuperHero")
                    .build();

            mockMvc.perform(post("/api/usuarios")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("Rol inválido"));
        }

        @Test
        @DisplayName("Admin updates another user's email and role")
        void adminUpdatesUsuarioSuccess() throws Exception {
            String token = tokenFor(adminUser);
            UsuarioUpdateDTO dto = UsuarioUpdateDTO.builder()
                    .email("vet_updated@sirbull.pe")
                    .rol("Administrador")
                    .build();

            mockMvc.perform(put("/api/usuarios/" + vetUser.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("vet_updated@sirbull.pe"))
                    .andExpect(jsonPath("$.rol").value("Administrador"));
        }

        @Test
        @DisplayName("Admin cannot deactivate own account (returns 400)")
        void adminCannotDeactivateOwnAccount() throws Exception {
            String token = tokenFor(adminUser);
            UsuarioUpdateDTO dto = UsuarioUpdateDTO.builder()
                    .isActive(false)
                    .build();

            mockMvc.perform(put("/api/usuarios/" + adminUser.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("No puedes desactivar tu propia cuenta"));
        }

        @Test
        @DisplayName("Admin cannot delete own account (returns 400)")
        void adminCannotDeleteOwnAccount() throws Exception {
            String token = tokenFor(adminUser);

            mockMvc.perform(delete("/api/usuarios/" + adminUser.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("No puedes eliminar tu propia cuenta"));
        }

        @Test
        @DisplayName("Admin deletes another user successfully")
        void adminDeletesAnotherUserSuccess() throws Exception {
            String token = tokenFor(adminUser);

            mockMvc.perform(delete("/api/usuarios/" + vetUser.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail").value("Usuario eliminado"));

            assertFalse(usuarioRepository.existsById(vetUser.getId()));
        }
    }

    @Nested
    @DisplayName("4. /api/registro-cliente Tests")
    class RegistroClienteTests {

        @Test
        @DisplayName("Registers new client atomically creates Usuario and Cliente")
        void registroClienteSuccess() throws Exception {
            RegistroClienteRequestDTO req = RegistroClienteRequestDTO.builder()
                    .username("newclient")
                    .password("pass123")
                    .dni("11223344")
                    .nombre("Maria Gomez")
                    .telefono("911223344")
                    .email("maria@gmail.com")
                    .build();

            mockMvc.perform(post("/api/registro-cliente")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail").value("Registro exitoso"));

            Optional<Usuario> userOpt = usuarioRepository.findByUsername("newclient");
            assertTrue(userOpt.isPresent());
            assertEquals(RolEnum.CLIENTE, userOpt.get().getRol());

            Optional<Cliente> clienteOpt = clienteRepository.findByDni("11223344");
            assertTrue(clienteOpt.isPresent());
            assertEquals("Maria Gomez", clienteOpt.get().getNombre());
            assertEquals(userOpt.get().getId(), clienteOpt.get().getUsuario().getId());
        }

        @Test
        @DisplayName("Duplicate username in registration returns 400")
        void registroClienteDuplicateUsernameFails() throws Exception {
            RegistroClienteRequestDTO req = RegistroClienteRequestDTO.builder()
                    .username("admin")
                    .password("pass123")
                    .dni("77889900")
                    .nombre("Otro")
                    .telefono("977889900")
                    .email("otro@gmail.com")
                    .build();

            mockMvc.perform(post("/api/registro-cliente")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("El usuario ya existe"));
        }

        @Test
        @DisplayName("Duplicate DNI or telefono returns 400")
        void registroClienteDuplicateDniOrTelefonoFails() throws Exception {
            RegistroClienteRequestDTO req = RegistroClienteRequestDTO.builder()
                    .username("differentuser")
                    .password("pass123")
                    .dni("87654321") // already belongs to clienteUser
                    .nombre("Duplicado DNI")
                    .telefono("900000000")
                    .email("dup@gmail.com")
                    .build();

            mockMvc.perform(post("/api/registro-cliente")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("DNI o Teléfono ya registrado"));
        }
    }

    @Nested
    @DisplayName("5. Password Recovery & Reset Flow Tests")
    class PasswordRecoveryTests {

        @Test
        @DisplayName("Recuperar password returns token in dev mode and invalidates prior tokens")
        void recuperarPasswordDevModeSuccess() throws Exception {
            RecuperarPasswordRequestDTO req = RecuperarPasswordRequestDTO.builder()
                    .email("vet@sirbull.pe")
                    .build();

            MvcResult result = mockMvc.perform(post("/api/recuperar-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail").value("Enlace generado correctamente."))
                    .andExpect(jsonPath("$.reset_token").isString())
                    .andExpect(jsonPath("$.reset_url").isString())
                    .andReturn();

            JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
            String token = node.get("reset_token").asText();

            // Request recovery again -> prior token marked used=true
            mockMvc.perform(post("/api/recuperar-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            // The first token should now be invalidated/used
            ResetPasswordRequestDTO resetReq = ResetPasswordRequestDTO.builder()
                    .token(token)
                    .newPassword("brandnewpass123")
                    .build();

            mockMvc.perform(post("/api/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetReq)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("El enlace es inválido o ya fue utilizado."));
        }

        @Test
        @DisplayName("Recuperar password for unknown email prevents account enumeration")
        void recuperarPasswordAntiEnumeration() throws Exception {
            RecuperarPasswordRequestDTO req = RecuperarPasswordRequestDTO.builder()
                    .email("nonexistent@sirbull.pe")
                    .build();

            mockMvc.perform(post("/api/recuperar-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail").value("Si el correo está registrado, recibirás un enlace de recuperación."))
                    .andExpect(jsonPath("$.reset_token").doesNotExist());
        }

        @Test
        @DisplayName("Reset password updates user password successfully and prevents re-use")
        void resetPasswordSuccessAndPreventReuse() throws Exception {
            RecuperarPasswordRequestDTO recReq = RecuperarPasswordRequestDTO.builder()
                    .email("vet@sirbull.pe")
                    .build();

            MvcResult result = mockMvc.perform(post("/api/recuperar-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(recReq)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
            String token = node.get("reset_token").asText();

            ResetPasswordRequestDTO resetReq = ResetPasswordRequestDTO.builder()
                    .token(token)
                    .newPassword("brandnewpass123")
                    .build();

            mockMvc.perform(post("/api/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.detail").value("Contraseña actualizada correctamente. Ya puedes iniciar sesión."));

            // Verify the user can log in with new password
            mockMvc.perform(post("/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "vet")
                            .param("password", "brandnewpass123"))
                    .andExpect(status().isOk());

            // Re-using the token must fail
            mockMvc.perform(post("/api/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetReq)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("El enlace es inválido o ya fue utilizado."));
        }

        @Test
        @DisplayName("Reset password with expired token fails with 400")
        void resetPasswordExpiredFails() throws Exception {
            RecuperarPasswordRequestDTO recReq = RecuperarPasswordRequestDTO.builder()
                    .email("vet@sirbull.pe")
                    .build();

            MvcResult result = mockMvc.perform(post("/api/recuperar-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(recReq)))
                    .andExpect(status().isOk())
                    .andReturn();

            JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
            String token = node.get("reset_token").asText();

            // Expire the token manually
            List<PasswordResetToken> tokens = passwordResetTokenRepository.findAll();
            for (PasswordResetToken t : tokens) {
                t.setExpiresAt(LocalDateTime.now().minusMinutes(10));
                passwordResetTokenRepository.save(t);
            }

            ResetPasswordRequestDTO resetReq = ResetPasswordRequestDTO.builder()
                    .token(token)
                    .newPassword("brandnewpass123")
                    .build();

            mockMvc.perform(post("/api/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetReq)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("El enlace ha expirado. Solicita uno nuevo."));
        }
    }

    @Nested
    @DisplayName("6. Auditoria Interceptor & Controller Tests")
    class AuditoriaTests {

        @Test
        @DisplayName("Mutating POST /api/usuarios records an Auditoria log automatically")
        void mutatingRequestRecordsAudit() throws Exception {
            String token = tokenFor(adminUser);
            UsuarioCreateDTO dto = UsuarioCreateDTO.builder()
                    .username("auditvet")
                    .password("secret123")
                    .rol("Veterinario")
                    .build();

            mockMvc.perform(post("/api/usuarios")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());

            // Check that auditoria has recorded the POST /api/usuarios
            List<Auditoria> logs = auditoriaRepository.findAll();
            boolean found = logs.stream().anyMatch(l ->
                    "POST".equals(l.getAccion()) &&
                    "/api/usuarios".equals(l.getEndpoint()) &&
                    l.getUsuario() != null &&
                    adminUser.getId().equals(l.getUsuario().getId())
            );
            assertTrue(found, "Auditoria record for POST /api/usuarios was not created by interceptor");
        }

        @Test
        @DisplayName("Admin can query /api/auditoria; non-admin is forbidden (403)")
        void adminCanQueryAuditoria() throws Exception {
            String adminToken = tokenFor(adminUser);
            String vetToken = tokenFor(vetUser);

            mockMvc.perform(get("/api/auditoria")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            mockMvc.perform(get("/api/auditoria")
                            .header("Authorization", "Bearer " + vetToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.detail").value("No tienes permisos suficientes para realizar esta acción"));
        }
    }
}
