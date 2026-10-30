package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.UsuarioCreateDTO;
import com.tecsolutions.Veterinaria.dto.UsuarioDTO;
import com.tecsolutions.Veterinaria.dto.UsuarioUpdateDTO;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crearUsuario(@RequestBody UsuarioCreateDTO dto) {
        return ResponseEntity.ok(usuarioService.crearUsuario(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(
            @PathVariable Long id,
            @RequestBody UsuarioUpdateDTO dto,
            @AuthenticationPrincipal Usuario currentUser) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, dto, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarUsuario(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario currentUser) {
        return ResponseEntity.ok(usuarioService.eliminarUsuario(id, currentUser));
    }
}
