package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.MarcaDTO;
import com.tecsolutions.Veterinaria.services.CatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marcas")
public class MarcaController {

    private final CatalogoService catalogoService;

    public MarcaController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<MarcaDTO>> listarMarcas() {
        return ResponseEntity.ok(catalogoService.listarMarcas());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<MarcaDTO> crearMarca(@RequestBody MarcaDTO dto) {
        return ResponseEntity.ok(catalogoService.crearMarca(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<MarcaDTO> actualizarMarca(
            @PathVariable Long id,
            @RequestBody MarcaDTO dto) {
        return ResponseEntity.ok(catalogoService.actualizarMarca(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarMarca(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.eliminarMarca(id));
    }
}
