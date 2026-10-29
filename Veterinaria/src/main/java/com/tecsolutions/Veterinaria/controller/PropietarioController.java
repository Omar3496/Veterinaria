package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.PropietarioDTO;
import com.tecsolutions.Veterinaria.services.PropietarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/propietarios")
public class PropietarioController {

    @Autowired
    private PropietarioService propietarioService;

    @GetMapping
    public List<PropietarioDTO> listarTodos() {
        return propietarioService.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropietarioDTO> buscarPorId(@PathVariable Long id) {
        return propietarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public PropietarioDTO crear(@RequestBody PropietarioDTO propietarioDTO) {
        return propietarioService.guardar(propietarioDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropietarioDTO> actualizar(@PathVariable Long id, @RequestBody PropietarioDTO propietarioDTO) {
        try {
            return ResponseEntity.ok(propietarioService.actualizar(id, propietarioDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        propietarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
