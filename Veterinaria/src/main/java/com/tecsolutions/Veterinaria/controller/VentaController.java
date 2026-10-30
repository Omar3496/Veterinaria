package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.*;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.services.VentaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping("/api/ventas")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA', 'Cliente', 'CLIENTE')")
    public ResponseEntity<VentaDTO> crearVenta(
            @RequestBody VentaCreateDTO dto,
            Authentication authentication) {
        String username = extractUsername(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(ventaService.crearVenta(dto, username, role));
    }

    @GetMapping("/api/ventas")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Veterinario', 'VETERINARIO', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<VentaDTO>> listarVentas(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(ventaService.listarVentas(skip, limit));
    }

    @GetMapping("/api/ventas/mias")
    @PreAuthorize("hasAnyRole('Cliente', 'CLIENTE')")
    public ResponseEntity<List<VentaDTO>> listarMisVentas(Authentication authentication) {
        String username = extractUsername(authentication);
        return ResponseEntity.ok(ventaService.listarMisVentas(username));
    }

    @GetMapping("/api/ventas/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Veterinario', 'VETERINARIO', 'Recepcionista', 'RECEPCIONISTA', 'Cliente', 'CLIENTE')")
    public ResponseEntity<VentaDTO> obtenerVenta(
            @PathVariable Long id,
            Authentication authentication) {
        String username = extractUsername(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(ventaService.obtenerVenta(id, username, role));
    }

    @PutMapping("/api/ventas/{id}/estado")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<VentaDTO> actualizarEstadoVenta(
            @PathVariable Long id,
            @RequestParam(required = false) String estado,
            @RequestBody(required = false) Map<String, Object> body,
            Authentication authentication) {
        String estadoFinal = estado;
        if ((estadoFinal == null || estadoFinal.isBlank()) && body != null && body.containsKey("estado")) {
            estadoFinal = String.valueOf(body.get("estado"));
        }
        String username = extractUsername(authentication);
        return ResponseEntity.ok(ventaService.actualizarEstadoVenta(id, estadoFinal, username));
    }

    @PostMapping("/api/ventas/{id}/solicitar-anulacion")
    @PreAuthorize("hasAnyRole('Cliente', 'CLIENTE')")
    public ResponseEntity<VentaDTO> solicitarAnulacion(
            @PathVariable Long id,
            Authentication authentication) {
        String username = extractUsername(authentication);
        return ResponseEntity.ok(ventaService.solicitarAnulacion(id, username));
    }

    @PostMapping("/api/ventas/{id}/devolver")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<VentaDTO> devolverVenta(
            @PathVariable Long id,
            @RequestBody DevolucionRequestDTO req,
            Authentication authentication) {
        String username = extractUsername(authentication);
        return ResponseEntity.ok(ventaService.devolverVenta(id, req, username));
    }

    @GetMapping("/api/notas-credito")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA', 'Cliente', 'CLIENTE')")
    public ResponseEntity<List<NotaCreditoDTO>> listarNotasCredito(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit,
            Authentication authentication) {
        String username = extractUsername(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(ventaService.listarNotasCredito(skip, limit, username, role));
    }

    @GetMapping("/api/devoluciones")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<List<DevolucionDTO>> listarDevoluciones(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(ventaService.listarDevoluciones(skip, limit));
    }

    private String extractUsername(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario.getUsername();
        }
        return authentication.getName();
    }

    private String extractRole(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof Usuario usuario && usuario.getRol() != null) {
            return usuario.getRol().getValue();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .findFirst()
                .orElse(null);
    }
}
