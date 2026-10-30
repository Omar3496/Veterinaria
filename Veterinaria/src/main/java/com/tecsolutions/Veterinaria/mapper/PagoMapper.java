package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.PagoDTO;
import com.tecsolutions.Veterinaria.model.Pago;
import org.springframework.stereotype.Component;

@Component
public class PagoMapper {

    public PagoDTO toDTO(Pago entity) {
        if (entity == null) {
            return null;
        }
        return PagoDTO.builder()
                .id(entity.getId())
                .citaId(entity.getCita() != null ? entity.getCita().getId() : null)
                .monto(entity.getMonto() != null ? entity.getMonto() : 0.0)
                .metodo(entity.getMetodo())
                .estado(entity.getEstado())
                .referencia(entity.getReferencia())
                .detalle(entity.getDetalle())
                .fecha(entity.getFecha())
                .tipoComprobante(entity.getTipoComprobante())
                .documentoFiscal(entity.getDocumentoFiscal())
                .serie(entity.getSerie())
                .numero(entity.getNumero())
                .comprobante(entity.getComprobante())
                .build();
    }
}
