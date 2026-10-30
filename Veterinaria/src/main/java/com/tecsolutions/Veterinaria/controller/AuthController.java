package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.RecuperarPasswordRequestDTO;
import com.tecsolutions.Veterinaria.dto.RegistroClienteRequestDTO;
import com.tecsolutions.Veterinaria.dto.ResetPasswordRequestDTO;
import com.tecsolutions.Veterinaria.dto.TokenDTO;
import com.tecsolutions.Veterinaria.services.AuthService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<TokenDTO> loginForm(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {
        TokenDTO token = authService.login(username, password);
        return ResponseEntity.ok(token);
    }

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenDTO> loginJson(@RequestBody Map<String, String> body) {
        String username = body != null ? body.get("username") : null;
        String password = body != null ? body.get("password") : null;
        TokenDTO token = authService.login(username, password);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/api/registro-cliente")
    public ResponseEntity<Map<String, String>> registrarCliente(@RequestBody RegistroClienteRequestDTO request) {
        Map<String, String> response = authService.registrarCliente(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/recuperar-password")
    public ResponseEntity<Map<String, Object>> recuperarPassword(@RequestBody RecuperarPasswordRequestDTO request) {
        Map<String, Object> response = authService.recuperarPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        Map<String, String> response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> readRoot() {
        return ResponseEntity.ok(Map.of("message", "Sirbull Pet API - Online"));
    }
}
