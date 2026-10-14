package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.model.Cita;
import com.tecsolutions.Veterinaria.services.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @GetMapping
    public List<Cita> listarTodas() {
        return citaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> buscarPorId(@PathVariable Long id) {
        return citaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mascota/{mascotaId}")
    public List<Cita> buscarPorMascota(@PathVariable Long mascotaId) {
        return citaService.buscarPorMascota(mascotaId);
    }

    @GetMapping("/veterinario/{veterinarioId}")
    public List<Cita> buscarPorVeterinario(@PathVariable Long veterinarioId) {
        return citaService.buscarPorVeterinario(veterinarioId);
    }

    @GetMapping("/estado/{estado}")
    public List<Cita> buscarPorEstado(@PathVariable String estado) {
        return citaService.buscarPorEstado(estado);
    }

    @PostMapping
    public Cita crear(@RequestBody Cita cita) {
        return citaService.guardar(cita);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizar(@PathVariable Long id, @RequestBody Cita cita) {
        try {
            return ResponseEntity.ok(citaService.actualizar(id, cita));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        citaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
