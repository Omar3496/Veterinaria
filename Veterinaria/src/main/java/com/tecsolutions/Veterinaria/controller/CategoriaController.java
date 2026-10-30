package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.CategoriaDTO;
import com.tecsolutions.Veterinaria.services.CatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CatalogoService catalogoService;

    public CategoriaController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<CategoriaDTO>> listarCategorias() {
        return ResponseEntity.ok(catalogoService.listarCategorias());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<CategoriaDTO> crearCategoria(@RequestBody CategoriaDTO dto) {
        return ResponseEntity.ok(catalogoService.crearCategoria(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<CategoriaDTO> actualizarCategoria(
            @PathVariable Long id,
            @RequestBody CategoriaDTO dto) {
        return ResponseEntity.ok(catalogoService.actualizarCategoria(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarCategoria(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.eliminarCategoria(id));
    }
}
