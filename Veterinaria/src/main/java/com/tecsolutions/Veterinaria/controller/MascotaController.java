package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.FotoRequestDTO;
import com.tecsolutions.Veterinaria.dto.MascotaDTO;
import com.tecsolutions.Veterinaria.security.SecurityUtils;
import com.tecsolutions.Veterinaria.services.MascotaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista', 'Cliente')")
    public ResponseEntity<List<MascotaDTO>> listarMascotas(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit,
            Authentication authentication) {
        String username = SecurityUtils.extractUsername(authentication);
        String role = SecurityUtils.extractRole(authentication);
        return ResponseEntity.ok(mascotaService.listarMascotas(skip, limit, username, role));
    }

    @PutMapping("/{id}/foto")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista', 'Cliente')")
    public ResponseEntity<MascotaDTO> actualizarFotoMascota(
            @PathVariable Long id,
            @RequestBody FotoRequestDTO req,
            Authentication authentication) {
        String username = SecurityUtils.extractUsername(authentication);
        String role = SecurityUtils.extractRole(authentication);
        return ResponseEntity.ok(mascotaService.actualizarFotoMascota(id, req.getFotoBase64(), username, role));
    }
}
