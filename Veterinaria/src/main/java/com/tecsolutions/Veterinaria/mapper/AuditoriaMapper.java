package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.AuditoriaDTO;
import com.tecsolutions.Veterinaria.model.Auditoria;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaMapper {

    public AuditoriaDTO toDTO(Auditoria auditoria) {
        if (auditoria == null) {
            return null;
        }
        return AuditoriaDTO.builder()
                .id(auditoria.getId())
                .usuarioId(auditoria.getUsuario() != null ? auditoria.getUsuario().getId() : null)
                .accion(auditoria.getAccion())
                .endpoint(auditoria.getEndpoint())
                .resourceId(auditoria.getResourceId())
                .timestamp(auditoria.getTimestamp())
                .detalle(auditoria.getDetalle())
                .build();
    }
}
