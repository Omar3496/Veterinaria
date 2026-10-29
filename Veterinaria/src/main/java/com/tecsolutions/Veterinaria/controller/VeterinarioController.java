package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.VeterinarioDTO;
import com.tecsolutions.Veterinaria.services.VeterinarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
public class VeterinarioController {

    @Autowired
    private VeterinarioService veterinarioService;

    @GetMapping
    public List<VeterinarioDTO> listarTodos() {
        return veterinarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeterinarioDTO> buscarPorId(@PathVariable Long id) {
        return veterinarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public VeterinarioDTO crear(@RequestBody VeterinarioDTO veterinarioDTO) {
        return veterinarioService.guardar(veterinarioDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeterinarioDTO> actualizar(@PathVariable Long id, @RequestBody VeterinarioDTO veterinarioDTO) {
        try {
            return ResponseEntity.ok(veterinarioService.actualizar(id, veterinarioDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        veterinarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
