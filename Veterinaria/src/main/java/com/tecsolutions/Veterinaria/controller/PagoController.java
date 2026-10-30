package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.PagoCreateDTO;
import com.tecsolutions.Veterinaria.dto.PagoDTO;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.services.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping("/api/citas/{cita_id}/pagos")
    @PreAuthorize("hasAnyRole('Administrador', 'Cliente')")
    public ResponseEntity<PagoDTO> createPagoCita(
            @PathVariable("cita_id") Long citaId,
            @RequestBody(required = false) PagoCreateDTO pago,
            Authentication authentication) {
        String username = extractUsername(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(pagoService.crearPagoCita(citaId, pago, username, role));
    }

    @PostMapping("/api/pagos/{id}/procesar")
    @PreAuthorize("hasAnyRole('Administrador', 'Cliente')")
    public ResponseEntity<PagoDTO> procesarPago(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> resultado,
            Authentication authentication) {
        String username = extractUsername(authentication);
        String role = extractRole(authentication);
        return ResponseEntity.ok(pagoService.procesarPago(id, resultado, username, role));
    }

    @GetMapping("/api/pagos/mios")
    @PreAuthorize("hasRole('Cliente')")
    public ResponseEntity<List<PagoDTO>> readMisPagos(Authentication authentication) {
        String username = extractUsername(authentication);
        return ResponseEntity.ok(pagoService.listarMisPagos(username));
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
        if (authentication.getPrincipal() instanceof Usuario usuario) {
            return usuario.getRol().getValue();
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
    }
}
