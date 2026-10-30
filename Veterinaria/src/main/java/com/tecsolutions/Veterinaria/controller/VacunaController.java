package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.RecordatorioVacunaDTO;
import com.tecsolutions.Veterinaria.dto.VacunaCreateDTO;
import com.tecsolutions.Veterinaria.dto.VacunaDTO;
import com.tecsolutions.Veterinaria.security.SecurityUtils;
import com.tecsolutions.Veterinaria.services.VacunaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class VacunaController {

    private final VacunaService vacunaService;

    public VacunaController(VacunaService vacunaService) {
        this.vacunaService = vacunaService;
    }

    @PostMapping("/api/mascotas/{mascotaId}/vacunas")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario')")
    public ResponseEntity<VacunaDTO> createVacuna(
            @PathVariable Long mascotaId,
            @RequestBody VacunaCreateDTO dto,
            Authentication authentication) {
        String username = SecurityUtils.extractUsername(authentication);
        return ResponseEntity.ok(vacunaService.crearVacuna(mascotaId, dto, username));
    }

    @GetMapping("/api/recordatorios")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Cliente')")
    public ResponseEntity<List<RecordatorioVacunaDTO>> getRecordatorios(Authentication authentication) {
        String username = SecurityUtils.extractUsername(authentication);
        String role = SecurityUtils.extractRole(authentication);
        return ResponseEntity.ok(vacunaService.obtenerRecordatorios(username, role));
    }
}
