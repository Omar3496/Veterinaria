package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.MovimientoStockDTO;
import com.tecsolutions.Veterinaria.services.MovimientoStockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos-stock")
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;

    public MovimientoStockController(MovimientoStockService movimientoStockService) {
        this.movimientoStockService = movimientoStockService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<MovimientoStockDTO>> listarMovimientos(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "300") int limit) {
        return ResponseEntity.ok(movimientoStockService.listarMovimientos(skip, limit));
    }
}
