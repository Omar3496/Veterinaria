package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.ExamenAuxiliarDTO;
import com.tecsolutions.Veterinaria.services.ExamenAuxiliarService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examenes-auxiliares")
public class ExamenAuxiliarController {

    private final ExamenAuxiliarService examenAuxiliarService;

    public ExamenAuxiliarController(ExamenAuxiliarService examenAuxiliarService) {
        this.examenAuxiliarService = examenAuxiliarService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista', 'Cliente')")
    public ResponseEntity<List<ExamenAuxiliarDTO>> readExamenesAuxiliares() {
        return ResponseEntity.ok(examenAuxiliarService.listarExamenes());
    }

    @PostMapping
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ExamenAuxiliarDTO> createExamenAuxiliar(@RequestBody ExamenAuxiliarDTO dto) {
        return ResponseEntity.ok(examenAuxiliarService.crearExamen(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<ExamenAuxiliarDTO> updateExamenAuxiliar(
            @PathVariable Long id,
            @RequestBody ExamenAuxiliarDTO dto) {
        return ResponseEntity.ok(examenAuxiliarService.actualizarExamen(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Map<String, Object>> deleteExamenAuxiliar(@PathVariable Long id) {
        return ResponseEntity.ok(examenAuxiliarService.eliminarExamen(id));
    }
}
