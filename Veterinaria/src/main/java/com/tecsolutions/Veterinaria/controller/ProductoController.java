package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.dto.CambioEstadoAlmacenRequestDTO;
import com.tecsolutions.Veterinaria.dto.NextSkuResponseDTO;
import com.tecsolutions.Veterinaria.dto.ProductoDTO;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.services.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarProductos(
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(productoService.listarProductos(skip, limit));
    }

    @GetMapping("/next-sku")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<NextSkuResponseDTO> obtenerSiguienteSku() {
        return ResponseEntity.ok(productoService.obtenerSiguienteSku());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<ProductoDTO> crearProducto(@RequestBody ProductoDTO dto) {
        return ResponseEntity.ok(productoService.crearProducto(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @PathVariable Long id,
            @RequestBody ProductoDTO dto,
            @AuthenticationPrincipal Usuario currentUser) {
        return ResponseEntity.ok(productoService.actualizarProducto(id, dto, currentUser));
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<ProductoDTO> actualizarStock(
            @PathVariable Long id,
            @RequestParam int cantidad,
            @RequestParam(defaultValue = "sumar") String operacion,
            @AuthenticationPrincipal Usuario currentUser) {
        return ResponseEntity.ok(productoService.actualizarStock(id, cantidad, operacion, currentUser));
    }

    @PutMapping("/{id}/estado-almacen")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN', 'Recepcionista', 'RECEPCIONISTA')")
    public ResponseEntity<ProductoDTO> cambiarEstadoAlmacen(
            @PathVariable Long id,
            @RequestBody CambioEstadoAlmacenRequestDTO request,
            @AuthenticationPrincipal Usuario currentUser) {
        return ResponseEntity.ok(productoService.cambiarEstadoAlmacen(id, request.getEstado(), request.getObservacion(), currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Administrador', 'ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.eliminarProducto(id));
    }
}
