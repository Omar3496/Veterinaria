package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.*;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.services.ConsultaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @PostMapping("/api/mascotas/{mascotaId}/consultas")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario')")
    public ResponseEntity<ConsultaDTO> crearConsulta(
            @PathVariable Long mascotaId,
            @RequestBody ConsultaCreateDTO dto,
            Authentication authentication) {
        String username = extractUsername(authentication);
        return ResponseEntity.ok(consultaService.crearConsulta(mascotaId, dto, username));
    }

    @PostMapping("/api/citas/{citaId}/atender")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario')")
    public ResponseEntity<ConsultaDTO> atenderCita(
            @PathVariable Long citaId,
            @RequestBody ConsultaCreateDTO dto,
            Authentication authentication) {
        String username = extractUsername(authentication);
        return ResponseEntity.ok(consultaService.atenderCita(citaId, dto, username));
    }

    @GetMapping("/api/mascotas/{mascotaId}/historial")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista', 'Cliente')")
    public ResponseEntity<HistorialMascotaDTO> readHistorialMascota(
            @PathVariable Long mascotaId,
            Authentication authentication) {
        String username = extractUsername(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(consultaService.obtenerHistorialMascota(mascotaId, username, role));
    }

    @GetMapping("/api/mascotas/{mascotaId}/recetas")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista', 'Cliente')")
    public ResponseEntity<List<RecetaMedicaDTO>> readRecetasMascota(
            @PathVariable Long mascotaId,
            Authentication authentication) {
        String username = extractUsername(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(consultaService.listarRecetasMascota(mascotaId, username, role));
    }

    @GetMapping("/api/mascotas/{mascotaId}/examenes")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista', 'Cliente')")
    public ResponseEntity<List<OrdenExamenDTO>> readExamenesMascota(
            @PathVariable Long mascotaId,
            Authentication authentication) {
        String username = extractUsername(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(consultaService.listarExamenesMascota(mascotaId, username, role));
    }

    @GetMapping("/api/mascotas/{mascotaId}/referencias")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista', 'Cliente')")
    public ResponseEntity<List<ReferenciaMedicaDTO>> readReferenciasMascota(
            @PathVariable Long mascotaId,
            Authentication authentication) {
        String username = extractUsername(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(consultaService.listarReferenciasMascota(mascotaId, username, role));
    }

    @PutMapping("/api/examenes/{ordenId}/resultado")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario')")
    public ResponseEntity<OrdenExamenDTO> updateOrdenExamenResultado(
            @PathVariable Long ordenId,
            @RequestBody OrdenExamenUpdateDTO dto) {
        return ResponseEntity.ok(consultaService.actualizarResultadoExamen(ordenId, dto));
    }

    @GetMapping("/api/ordenes-medicas/mias")
    @PreAuthorize("hasRole('Cliente')")
    public ResponseEntity<OrdenesMedicasClienteDTO> readMisOrdenesMedicas(Authentication authentication) {
        String username = extractUsername(authentication);
        return ResponseEntity.ok(consultaService.obtenerMisOrdenesMedicas(username));
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
