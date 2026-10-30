package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.HorarioConfigDTO;
import com.tecsolutions.Veterinaria.services.ConfiguracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping("/horarios")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista', 'Cliente')")
    public ResponseEntity<HorarioConfigDTO> readHorariosAtencion() {
        return ResponseEntity.ok(configuracionService.obtenerHorarios());
    }

    @PutMapping("/horarios")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista')")
    public ResponseEntity<HorarioConfigDTO> updateHorariosAtencion(@RequestBody HorarioConfigDTO config) {
        return ResponseEntity.ok(configuracionService.actualizarHorarios(config));
    }

    @GetMapping("/pago")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista', 'Cliente')")
    public ResponseEntity<Map<String, Object>> readConfigPago() {
        return ResponseEntity.ok(configuracionService.obtenerConfigPago());
    }

    @PutMapping("/pago")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Map<String, Object>> updateConfigPago(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(configuracionService.actualizarConfigPago(payload));
    }
}
