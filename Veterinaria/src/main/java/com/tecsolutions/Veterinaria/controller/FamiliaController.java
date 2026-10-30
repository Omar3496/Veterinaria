package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.FamiliaDTO;
import com.tecsolutions.Veterinaria.services.CatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/familias")
public class FamiliaController {

    private final CatalogoService catalogoService;

    public FamiliaController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<FamiliaDTO>> listarFamilias() {
        return ResponseEntity.ok(catalogoService.listarFamilias());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<FamiliaDTO> crearFamilia(@RequestBody FamiliaDTO dto) {
        return ResponseEntity.ok(catalogoService.crearFamilia(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<FamiliaDTO> actualizarFamilia(
            @PathVariable Long id,
            @RequestBody FamiliaDTO dto) {
        return ResponseEntity.ok(catalogoService.actualizarFamilia(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarFamilia(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.eliminarFamilia(id));
    }
}
