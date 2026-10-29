package com.tecsolutions.Veterinaria.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tecsolutions.Veterinaria.dto.CitaDTO;
import com.tecsolutions.Veterinaria.services.CitaService;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    @GetMapping
    public List<CitaDTO> listarTodas() {
        return citaService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitaDTO> buscarPorId(@PathVariable Long id) {
        return citaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mascota/{mascotaId}")
    public List<CitaDTO> buscarPorMascota(@PathVariable Long mascotaId) {
        return citaService.buscarPorMascota(mascotaId);
    }

    @GetMapping("/veterinario/{veterinarioId}")
    public List<CitaDTO> buscarPorVeterinario(@PathVariable Long veterinarioId) {
        return citaService.buscarPorVeterinario(veterinarioId);
    }

    @GetMapping("/estado/{estado}")
    public List<CitaDTO> buscarPorEstado(@PathVariable String estado) {
        return citaService.buscarPorEstado(estado);
    }

    @PostMapping
    public CitaDTO crear(@RequestBody CitaDTO citaDTO) {
        return citaService.guardar(citaDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CitaDTO> actualizar(@PathVariable Long id, @RequestBody CitaDTO citaDTO) {
        try {
            return ResponseEntity.ok(citaService.actualizar(id, citaDTO));
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
