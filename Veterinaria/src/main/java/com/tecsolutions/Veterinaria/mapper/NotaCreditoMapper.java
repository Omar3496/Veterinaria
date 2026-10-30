package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.NotaCreditoDTO;
import com.tecsolutions.Veterinaria.model.NotaCredito;
import org.springframework.stereotype.Component;

@Component
public class NotaCreditoMapper {

    public NotaCreditoDTO toDTO(NotaCredito entity) {
        if (entity == null) {
            return null;
        }

        return NotaCreditoDTO.builder()
                .id(entity.getId())
                .codigo(entity.getCodigo())
                .clienteId(entity.getCliente() != null ? entity.getCliente().getId() : null)
                .clienteNombre(entity.getClienteNombre())
                .ventaId(entity.getVentaId())
                .monto(entity.getMonto())
                .saldo(entity.getSaldo())
                .fechaEmision(entity.getFechaEmision())
                .fechaVencimiento(entity.getFechaVencimiento())
                .estado(entity.getEstado())
                .motivo(entity.getMotivo())
                .vigente(entity.isVigente())
                .build();
    }
}
