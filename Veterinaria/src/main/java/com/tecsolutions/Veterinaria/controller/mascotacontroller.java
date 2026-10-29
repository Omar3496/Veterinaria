package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.model.Mascota;
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
    public List<Mascota> listarTodas() {
        return mascotaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> buscarPorId(@PathVariable Long id) {
        return mascotaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/propietario/{propietarioId}")
    public List<Mascota> buscarPorPropietario(@PathVariable Long propietarioId) {
        return mascotaService.buscarPorPropietario(propietarioId);
    }

    @GetMapping("/especie/{especie}")
    public List<Mascota> buscarPorEspecie(@PathVariable String especie) {
        return mascotaService.buscarPorEspecie(especie);
    }

    @PostMapping
    public Mascota crear(@RequestBody Mascota mascota) {
        return mascotaService.guardar(mascota);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizar(@PathVariable Long id, @RequestBody Mascota mascota) {
        try {
            return ResponseEntity.ok(mascotaService.actualizar(id, mascota));
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
