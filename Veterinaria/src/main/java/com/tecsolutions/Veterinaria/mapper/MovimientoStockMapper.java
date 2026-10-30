package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.MovimientoStockDTO;
import com.tecsolutions.Veterinaria.model.MovimientoStock;
import org.springframework.stereotype.Component;

@Component
public class MovimientoStockMapper {

    public MovimientoStockDTO toDTO(MovimientoStock entity) {
        if (entity == null) {
            return null;
        }
        return MovimientoStockDTO.builder()
                .id(entity.getId())
                .productoId(entity.getProducto() != null ? entity.getProducto().getId() : null)
                .productoNombre(entity.getProductoNombre())
                .fecha(entity.getFecha())
                .tipoMovimiento(entity.getTipoMovimiento())
                .cantidad(entity.getCantidad())
                .estadoAnterior(entity.getEstadoAnterior())
                .estadoNuevo(entity.getEstadoNuevo())
                .observacion(entity.getObservacion())
                .referencia(entity.getReferencia())
                .usuarioId(entity.getUsuario() != null ? entity.getUsuario().getId() : null)
                .usuarioNombre(entity.getUsuarioNombre())
                .build();
    }
}
