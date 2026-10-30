package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.IngresoDTO;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.services.IngresoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ingresos")
public class IngresoController {

    private final IngresoService ingresoService;

    public IngresoController(IngresoService ingresoService) {
        this.ingresoService = ingresoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<IngresoDTO>> listarIngresos(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(ingresoService.listarIngresos(skip, limit));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<IngresoDTO> crearIngreso(
            @RequestBody IngresoDTO dto,
            @AuthenticationPrincipal Usuario currentUser) {
        return ResponseEntity.ok(ingresoService.crearIngreso(dto, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarIngreso(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario currentUser) {
        return ResponseEntity.ok(ingresoService.eliminarIngreso(id, currentUser));
    }
}
