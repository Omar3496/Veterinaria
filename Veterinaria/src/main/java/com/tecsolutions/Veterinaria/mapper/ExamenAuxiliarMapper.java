package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.ExamenAuxiliarDTO;
import com.tecsolutions.Veterinaria.model.ExamenAuxiliar;
import org.springframework.stereotype.Component;

@Component
public class ExamenAuxiliarMapper {

    public ExamenAuxiliarDTO toDTO(ExamenAuxiliar entity) {
        if (entity == null) {
            return null;
        }
        return ExamenAuxiliarDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .precio(entity.getPrecio() != null ? entity.getPrecio() : 0.0)
                .tiempoEstimado(entity.getTiempoEstimado())
                .activo(entity.getActivo() != null ? entity.getActivo() : true)
                .build();
    }

    public ExamenAuxiliar toEntity(ExamenAuxiliarDTO dto) {
        if (dto == null) {
            return null;
        }
        ExamenAuxiliar entity = new ExamenAuxiliar();
        entity.setId(dto.getId());
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setPrecio(dto.getPrecio() != null ? dto.getPrecio() : 0.0);
        entity.setTiempoEstimado(dto.getTiempoEstimado());
        entity.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return entity;
    }
}
