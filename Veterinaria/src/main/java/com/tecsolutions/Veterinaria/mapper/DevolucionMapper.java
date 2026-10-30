package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.DevolucionDTO;
import com.tecsolutions.Veterinaria.model.Devolucion;
import org.springframework.stereotype.Component;

@Component
public class DevolucionMapper {

    public DevolucionDTO toDTO(Devolucion entity) {
        if (entity == null) {
            return null;
        }

        return DevolucionDTO.builder()
                .id(entity.getId())
                .ventaId(entity.getVenta() != null ? entity.getVenta().getId() : null)
                .clienteId(entity.getCliente() != null ? entity.getCliente().getId() : null)
                .tipo(entity.getTipo())
                .monto(entity.getMonto())
                .motivo(entity.getMotivo())
                .fecha(entity.getFecha())
                .usuarioId(entity.getUsuario() != null ? entity.getUsuario().getId() : null)
                .notaCreditoId(entity.getNotaCreditoId())
                .build();
    }
}
