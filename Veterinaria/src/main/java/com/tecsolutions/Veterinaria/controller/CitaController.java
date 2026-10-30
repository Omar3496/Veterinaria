package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.*;
import com.tecsolutions.Veterinaria.security.SecurityUtils;
import com.tecsolutions.Veterinaria.services.CitaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping("/disponibilidad")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista', 'Cliente')")
    public ResponseEntity<DisponibilidadDiaDTO> getDisponibilidadDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false, name = "veterinario_id") Long veterinarioId) {
        return ResponseEntity.ok(citaService.obtenerDisponibilidad(fecha, veterinarioId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista')")
    public ResponseEntity<List<CitaDTO>> readCitas(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(citaService.listarCitas(skip, limit));
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('Cliente')")
    public ResponseEntity<List<CitaConMascotaDTO>> readCitasMias(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit,
            Authentication authentication) {
        String username = SecurityUtils.extractUsername(authentication);
        return ResponseEntity.ok(citaService.listarCitasMias(skip, limit, username));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista', 'Cliente')")
    public ResponseEntity<CitaDTO> createCita(
            @RequestBody CitaDTO dto,
            Authentication authentication) {
        String username = SecurityUtils.extractUsername(authentication);
        String role = SecurityUtils.extractRole(authentication);
        return ResponseEntity.ok(citaService.agendarCita(dto, username, role));
    }

    @PutMapping("/{id}/reprogramar")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista', 'Cliente')")
    public ResponseEntity<CitaDTO> reprogramarCita(
            @PathVariable Long id,
            @RequestBody ReprogramarCitaRequestDTO req,
            Authentication authentication) {
        String username = SecurityUtils.extractUsername(authentication);
        String role = SecurityUtils.extractRole(authentication);
        return ResponseEntity.ok(citaService.reprogramarCita(id, req, username, role));
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('Cliente')")
    public ResponseEntity<CitaDTO> cancelarCitaCliente(
            @PathVariable Long id,
            @RequestBody CancelarCitaRequestDTO req,
            Authentication authentication) {
        String username = SecurityUtils.extractUsername(authentication);
        return ResponseEntity.ok(citaService.cancelarCitaCliente(id, req, username));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista')")
    public ResponseEntity<CitaDTO> updateCitaEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestParam(required = false, name = "motivo_cancelacion") String motivoCancelacion) {
        return ResponseEntity.ok(citaService.actualizarEstadoCita(id, estado, motivoCancelacion));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario')")
    public ResponseEntity<Map<String, Object>> deleteCita(@PathVariable Long id) {
        return ResponseEntity.ok(citaService.eliminarCita(id));
    }
}
