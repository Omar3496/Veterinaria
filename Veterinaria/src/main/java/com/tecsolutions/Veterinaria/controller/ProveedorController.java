package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.ProveedorDTO;
import com.tecsolutions.Veterinaria.services.CatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final CatalogoService catalogoService;

    public ProveedorController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<ProveedorDTO>> listarProveedores(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(catalogoService.listarProveedores(skip, limit));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<ProveedorDTO> crearProveedor(@RequestBody ProveedorDTO dto) {
        return ResponseEntity.ok(catalogoService.crearProveedor(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<ProveedorDTO> actualizarProveedor(
            @PathVariable Long id,
            @RequestBody ProveedorDTO dto) {
        return ResponseEntity.ok(catalogoService.actualizarProveedor(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarProveedor(@PathVariable Long id) {
        return ResponseEntity.ok(catalogoService.eliminarProveedor(id));
    }
}
