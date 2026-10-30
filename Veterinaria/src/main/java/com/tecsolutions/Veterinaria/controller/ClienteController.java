package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.ClienteDTO;
import com.tecsolutions.Veterinaria.dto.FotoRequestDTO;
import com.tecsolutions.Veterinaria.dto.MascotaDTO;
import com.tecsolutions.Veterinaria.security.SecurityUtils;
import com.tecsolutions.Veterinaria.services.ClienteService;
import com.tecsolutions.Veterinaria.services.MascotaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final MascotaService mascotaService;

    public ClienteController(ClienteService clienteService, MascotaService mascotaService) {
        this.clienteService = clienteService;
        this.mascotaService = mascotaService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('Cliente')")
    public ResponseEntity<ClienteDTO> obtenerClienteActual(Authentication authentication) {
        String username = SecurityUtils.extractUsername(authentication);
        return ResponseEntity.ok(clienteService.obtenerClienteActual(username));
    }

    @PutMapping("/me/foto")
    @PreAuthorize("hasRole('Cliente')")
    public ResponseEntity<ClienteDTO> actualizarFotoClienteActual(
            @RequestBody FotoRequestDTO req,
            Authentication authentication) {
        String username = SecurityUtils.extractUsername(authentication);
        return ResponseEntity.ok(clienteService.actualizarFotoClienteActual(username, req.getFotoBase64()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista')")
    public ResponseEntity<List<ClienteDTO>> listarClientes(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(clienteService.listarClientes(skip, limit));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista')")
    public ResponseEntity<ClienteDTO> crearCliente(@RequestBody ClienteDTO dto) {
        return ResponseEntity.ok(clienteService.crearCliente(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Map<String, String>> eliminarCliente(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.eliminarCliente(id));
    }

    @PostMapping("/{cliente_id}/mascotas")
    @PreAuthorize("hasAnyRole('Administrador', 'Veterinario', 'Recepcionista')")
    public ResponseEntity<MascotaDTO> crearMascotaParaCliente(
            @PathVariable("cliente_id") Long clienteId,
            @RequestBody MascotaDTO dto) {
        return ResponseEntity.ok(mascotaService.crearMascotaParaCliente(clienteId, dto));
    }
}
