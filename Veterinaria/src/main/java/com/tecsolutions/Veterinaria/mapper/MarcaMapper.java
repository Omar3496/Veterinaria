package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.MarcaDTO;
import com.tecsolutions.Veterinaria.model.Marca;
import org.springframework.stereotype.Component;

@Component
public class MarcaMapper {

    public MarcaDTO toDTO(Marca entity) {
        if (entity == null) {
            return null;
        }
        return MarcaDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .build();
    }

    public Marca toEntity(MarcaDTO dto) {
        if (dto == null) {
            return null;
        }
        Marca entity = new Marca();
        entity.setId(dto.getId());
        entity.setNombre(dto.getNombre());
        return entity;
    }
}
