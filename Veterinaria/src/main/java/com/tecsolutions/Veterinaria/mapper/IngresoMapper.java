package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.IngresoDTO;
import com.tecsolutions.Veterinaria.dto.IngresoDetalleDTO;
import com.tecsolutions.Veterinaria.model.Ingreso;
import com.tecsolutions.Veterinaria.model.IngresoDetalle;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class IngresoMapper {

    public IngresoDTO toDTO(Ingreso entity) {
        if (entity == null) {
            return null;
        }

        List<IngresoDetalleDTO> detalles = new ArrayList<>();
        if (entity.getDetalles() != null) {
            detalles = entity.getDetalles().stream()
                    .map(this::toDetalleDTO)
                    .toList();
        }

        return IngresoDTO.builder()
                .id(entity.getId())
                .fecha(entity.getFecha())
                .proveedorId(entity.getProveedor() != null ? entity.getProveedor().getId() : null)
                .proveedorNombre(entity.getProveedorNombre())
                .tipoPago(entity.getTipoPago())
                .tipoComprobante(entity.getTipoComprobante())
                .serie(entity.getSerie())
                .numero(entity.getNumero())
                .numeroFactura(entity.getNumeroFactura())
                .total(entity.getTotal())
                .usuarioId(entity.getUsuario() != null ? entity.getUsuario().getId() : null)
                .detalles(detalles)
                .build();
    }

    public IngresoDetalleDTO toDetalleDTO(IngresoDetalle entity) {
        if (entity == null) {
            return null;
        }
        return IngresoDetalleDTO.builder()
                .id(entity.getId())
                .productoId(entity.getProducto() != null ? entity.getProducto().getId() : null)
                .productoNombre(entity.getProductoNombre())
                .cantidad(entity.getCantidad())
                .precioUnitario(entity.getPrecioUnitario())
                .precioVenta(entity.getPrecioVenta())
                .fechaVencimiento(entity.getFechaVencimiento())
                .subtotal(entity.getSubtotal())
                .build();
    }
}
