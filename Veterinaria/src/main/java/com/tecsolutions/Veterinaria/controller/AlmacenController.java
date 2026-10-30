package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.MovimientoAlmacenRequestDTO;
import com.tecsolutions.Veterinaria.dto.ProductoDTO;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.services.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/almacenes")
public class AlmacenController {

    private final ProductoService productoService;

    public AlmacenController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping("/mover")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<ProductoDTO> moverStock(
            @RequestBody MovimientoAlmacenRequestDTO request,
            @AuthenticationPrincipal Usuario currentUser) {
        return ResponseEntity.ok(productoService.moverStock(request, currentUser));
    }
}
