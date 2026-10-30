package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.AuditoriaDTO;
import com.tecsolutions.Veterinaria.services.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<List<AuditoriaDTO>> listarAuditoria() {
        return ResponseEntity.ok(auditoriaService.listarAuditoria());
    }
}
