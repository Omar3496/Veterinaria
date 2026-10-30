package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.RecuperarPasswordRequestDTO;
import com.tecsolutions.Veterinaria.dto.RegistroClienteRequestDTO;
import com.tecsolutions.Veterinaria.dto.ResetPasswordRequestDTO;
import com.tecsolutions.Veterinaria.dto.TokenDTO;
import com.tecsolutions.Veterinaria.model.Cliente;
import com.tecsolutions.Veterinaria.model.PasswordResetToken;
import com.tecsolutions.Veterinaria.model.RolEnum;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.repository.ClienteRepository;
import com.tecsolutions.Veterinaria.repository.PasswordResetTokenRepository;
import com.tecsolutions.Veterinaria.repository.UsuarioRepository;
import com.tecsolutions.Veterinaria.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditoriaService auditoriaService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditoriaService = auditoriaService;
    }

    public TokenDTO login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank() || password == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }

        Usuario user = buscarUsuarioPorIdentificador(usernameOrEmail);
        if (user == null || !Boolean.TRUE.equals(user.getIsActive()) || !passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRol().getValue());

        try {
            auditoriaService.registrarAccion(user, "LOGIN", "/token", null, "Inicio de sesión exitoso");
        } catch (Exception ignored) {
        }

        return TokenDTO.builder()
                .accessToken(token)
                .tokenType("bearer")
                .build();
    }

    public Map<String, String> registrarCliente(RegistroClienteRequestDTO req) {
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre de usuario requerido");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseña requerida");
        }

        if (usuarioRepository.existsByUsername(req.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario ya existe");
        }

        if (clienteRepository.existsByDniOrTelefono(req.getDni(), req.getTelefono())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DNI o Teléfono ya registrado");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(req.getUsername());
        nuevoUsuario.setHashedPassword(passwordEncoder.encode(req.getPassword()));
        nuevoUsuario.setRol(RolEnum.CLIENTE);
        nuevoUsuario.setIsActive(true);
        nuevoUsuario.setEmail(req.getEmail());
        nuevoUsuario = usuarioRepository.save(nuevoUsuario);

        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setDni(req.getDni());
        nuevoCliente.setNombre(req.getNombre());
        nuevoCliente.setTelefono(req.getTelefono());
        nuevoCliente.setEmail(req.getEmail());
        nuevoCliente.setUsuario(nuevoUsuario);
        clienteRepository.save(nuevoCliente);

        return Map.of("detail", "Registro exitoso");
    }

    public Map<String, Object> recuperarPassword(RecuperarPasswordRequestDTO req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            return Map.of("detail", "Si el correo está registrado, recibirás un enlace de recuperación.");
        }

        Usuario usuario = buscarUsuarioPorEmail(req.getEmail());
        if (usuario == null || !Boolean.TRUE.equals(usuario.getIsActive())) {
            return Map.of("detail", "Si el correo está registrado, recibirás un enlace de recuperación.");
        }

        // Invalidar tokens previos no usados
        List<PasswordResetToken> priorTokens = passwordResetTokenRepository.findByUsuarioIdAndUsedFalse(usuario.getId());
        for (PasswordResetToken pt : priorTokens) {
            pt.setUsed(true);
        }
        passwordResetTokenRepository.saveAll(priorTokens);

        // Generar token seguro y hash SHA-256
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        String tokenHash = sha256Hex(token);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUsuario(usuario);
        resetToken.setTokenHash(tokenHash);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        resetToken.setUsed(false);
        resetToken.setCreatedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null || frontendUrl.isBlank()) {
            frontendUrl = "http://localhost:5173";
        }
        String link = frontendUrl + "/reset-password?token=" + token;

        guardarCorreoReset(usuario, link);

        boolean isDev = !"prod".equalsIgnoreCase(System.getenv("APP_ENV"));
        if (isDev) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("detail", "Enlace generado correctamente.");
            response.put("reset_token", token);
            response.put("reset_url", link);
            return response;
        }

        return Map.of("detail", "Si el correo está registrado, recibirás un enlace de recuperación.");
    }

    public Map<String, String> resetPassword(ResetPasswordRequestDTO req) {
        if (req.getToken() == null || req.getToken().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace es inválido o ya fue utilizado.");
        }

        String tokenHash = sha256Hex(req.getToken());
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenHash(tokenHash);
        if (tokenOpt.isEmpty() || Boolean.TRUE.equals(tokenOpt.get().getUsed())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace es inválido o ya fue utilizado.");
        }

        PasswordResetToken reset = tokenOpt.get();
        if (reset.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace ha expirado. Solicita uno nuevo.");
        }

        Usuario usuario = reset.getUsuario();
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El enlace es inválido.");
        }

        usuario.setHashedPassword(passwordEncoder.encode(req.getNewPassword()));
        reset.setUsed(true);

        usuarioRepository.save(usuario);
        passwordResetTokenRepository.save(reset);

        return Map.of("detail", "Contraseña actualizada correctamente. Ya puedes iniciar sesión.");
    }

    public Usuario buscarUsuarioPorIdentificador(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }
        String trimmed = identifier.trim();
        Optional<Usuario> userOpt = usuarioRepository.findByUsername(trimmed);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        return buscarUsuarioPorEmail(trimmed);
    }

    public Usuario buscarUsuarioPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim().toLowerCase();
        Optional<Usuario> userOpt = usuarioRepository.findByEmailIgnoreCase(trimmed);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        Optional<Cliente> clienteOpt = clienteRepository.findByEmailIgnoreCase(trimmed);
        if (clienteOpt.isPresent() && clienteOpt.get().getUsuario() != null) {
            return clienteOpt.get().getUsuario();
        }
        return null;
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular hash SHA-256", e);
        }
    }

    private void guardarCorreoReset(Usuario usuario, String link) {
        try {
            File emailsDir = new File("emails");
            if (!emailsDir.exists()) {
                emailsDir.mkdirs();
            }
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File file = new File(emailsDir, String.format("reset_%s_%s.html", usuario.getUsername(), ts));
            String html = String.format("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head><meta charset="utf-8"><title>Recuperar contraseña - Sirbull Pet Clinic</title></head>
                    <body style="font-family:Arial,sans-serif;background:#f5f5f5;padding:24px">
                      <div style="max-width:520px;margin:auto;background:#fff;border-radius:12px;padding:28px;border:1px solid #e5e7eb">
                        <h2 style="margin:0 0 12px;color:#065f46">Recuperación de contraseña</h2>
                        <p style="color:#374151">Hola <b>%s</b>, recibimos una solicitud para restablecer tu contraseña.</p>
                        <p style="color:#374151">El enlace es válido por <b>30 minutos</b>. Si no fuiste tú, ignora este correo.</p>
                        <p style="text-align:center;margin:24px 0">
                          <a href="%s" style="background:#047857;color:#fff;padding:12px 22px;border-radius:10px;text-decoration:none;font-weight:bold">Restablecer contraseña</a>
                        </p>
                        <p style="font-size:12px;color:#9ca3af">Sirbull Pet Clinic</p>
                      </div>
                    </body>
                    </html>
                    """, usuario.getUsername(), link);
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write(html);
            }
        } catch (IOException ignored) {
        }
    }
}
