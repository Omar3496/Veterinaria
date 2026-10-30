package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.RecordatorioVacunaDTO;
import com.tecsolutions.Veterinaria.dto.VacunaCreateDTO;
import com.tecsolutions.Veterinaria.dto.VacunaDTO;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.services.VacunaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
        String username = extractUsername(authentication);
        return ResponseEntity.ok(vacunaService.crearVacuna(mascotaId, dto, username));
    }

    @GetMapping("/api/recordatorios")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Cliente')")
    public ResponseEntity<List<RecordatorioVacunaDTO>> getRecordatorios(Authentication authentication) {
        String username = extractUsername(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(vacunaService.obtenerRecordatorios(username, role));
    }

    private String extractUsername(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario.getUsername();
        }
        return authentication.getName();
    }

    private String extractRole(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario.getRol().getValue();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }
}
