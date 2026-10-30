package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.*;
import com.tecsolutions.Veterinaria.services.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/api/stats")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Veterinario', 'VETERINARIO')")
    public ResponseEntity<StatsResponseDTO> getStats() {
        return ResponseEntity.ok(reporteService.obtenerStats());
    }

    @GetMapping("/api/citas/proximas")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Veterinario', 'VETERINARIO', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<ProximaCitaDTO>> getProximasCitas() {
        return ResponseEntity.ok(reporteService.obtenerProximasCitas());
    }

    @GetMapping("/api/reportes/ventas")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteVentaItemDTO>> reporteVentas(
            @RequestParam(name = "fecha_inicio", required = false) String fechaInicio,
            @RequestParam(name = "fecha_fin", required = false) String fechaFin) {
        LocalDate start = parseDate(fechaInicio);
        LocalDate end = parseDate(fechaFin);
        return ResponseEntity.ok(reporteService.reporteVentas(start, end));
    }

    @GetMapping("/api/reportes/productos")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteProductoItemDTO>> reporteProductos(
            @RequestParam(name = "fecha_inicio", required = false) String fechaInicio,
            @RequestParam(name = "fecha_fin", required = false) String fechaFin) {
        LocalDate start = parseDate(fechaInicio);
        LocalDate end = parseDate(fechaFin);
        return ResponseEntity.ok(reporteService.reporteProductos(start, end));
    }

    @GetMapping("/api/reportes/citas")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<ReporteCitaMesItemDTO>> reporteCitas(
            @RequestParam(name = "fecha_inicio", required = false) String fechaInicio,
            @RequestParam(name = "fecha_fin", required = false) String fechaFin) {
        LocalDate start = parseDate(fechaInicio);
        LocalDate end = parseDate(fechaFin);
        return ResponseEntity.ok(reporteService.reporteCitas(start, end));
    }

    @GetMapping("/api/reportes/demografia")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<DemografiaItemDTO>> reporteDemografia() {
        return ResponseEntity.ok(reporteService.reporteDemografia());
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            String trimmed = dateStr.trim();
            if (trimmed.length() >= 10) {
                return LocalDate.parse(trimmed.substring(0, 10));
            }
            return LocalDate.parse(trimmed);
        } catch (Exception e) {
            return null;
        }
    }
}
