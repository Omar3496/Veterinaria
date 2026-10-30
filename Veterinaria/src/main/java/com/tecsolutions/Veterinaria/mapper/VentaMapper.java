package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.DetalleVentaDTO;
import com.tecsolutions.Veterinaria.dto.VentaDTO;
import com.tecsolutions.Veterinaria.model.DetalleVenta;
import com.tecsolutions.Veterinaria.model.Venta;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VentaMapper {

    public VentaDTO toDTO(Venta entity) {
        if (entity == null) {
            return null;
        }

        List<DetalleVentaDTO> detalles = new ArrayList<>();
        if (entity.getDetalles() != null) {
            detalles = entity.getDetalles().stream()
                    .map(this::toDetalleDTO)
                    .toList();
        }

        return VentaDTO.builder()
                .id(entity.getId())
                .fecha(entity.getFecha())
                .clienteId(entity.getCliente() != null ? entity.getCliente().getId() : null)
                .clienteNombre(entity.getClienteNombre())
                .usuarioId(entity.getUsuario() != null ? entity.getUsuario().getId() : null)
                .vendedorNombre(entity.getVendedorNombre())
                .total(entity.getTotal())
                .metodoPago(entity.getMetodoPago())
                .tipoComprobante(entity.getTipoComprobante())
                .documentoFiscal(entity.getDocumentoFiscal())
                .serie(entity.getSerie())
                .numero(entity.getNumero())
                .comprobante(entity.getComprobante())
                .codigoRecojo(entity.getCodigoRecojo())
                .estado(entity.getEstado() != null ? entity.getEstado().getValue() : null)
                .estadoNombre(entity.getEstadoNombre())
                .notaCreditoId(entity.getNotaCreditoId())
                .montoNota(entity.getMontoNota() != null ? entity.getMontoNota() : 0.0)
                .detalles(detalles)
                .build();
    }

    public DetalleVentaDTO toDetalleDTO(DetalleVenta entity) {
        if (entity == null) {
            return null;
        }
        return DetalleVentaDTO.builder()
                .id(entity.getId())
                .productoId(entity.getProducto() != null ? entity.getProducto().getId() : null)
                .productoNombre(entity.getProductoNombre())
                .cantidad(entity.getCantidad())
                .precioUnitario(entity.getPrecioUnitario())
                .subtotal(entity.getSubtotal())
                .build();
    }
}
