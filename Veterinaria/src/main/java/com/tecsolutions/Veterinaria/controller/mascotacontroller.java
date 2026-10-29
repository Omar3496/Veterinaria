package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.MascotaDTO;
import com.tecsolutions.Veterinaria.services.MascotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    @Autowired
    private MascotaService mascotaService;

    @GetMapping
    public List<MascotaDTO> listarTodas() {
        return mascotaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MascotaDTO> buscarPorId(@PathVariable Long id) {
        return mascotaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/propietario/{propietarioId}")
    public List<MascotaDTO> buscarPorPropietario(@PathVariable Long propietarioId) {
        return mascotaService.buscarPorPropietario(propietarioId);
    }

    @GetMapping("/especie/{especie}")
    public List<MascotaDTO> buscarPorEspecie(@PathVariable String especie) {
        return mascotaService.buscarPorEspecie(especie);
    }

    @PostMapping
    public MascotaDTO crear(@RequestBody MascotaDTO mascotaDTO) {
        return mascotaService.guardar(mascotaDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MascotaDTO> actualizar(@PathVariable Long id, @RequestBody MascotaDTO mascotaDTO) {
        try {
            return ResponseEntity.ok(mascotaService.actualizar(id, mascotaDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mascotaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
