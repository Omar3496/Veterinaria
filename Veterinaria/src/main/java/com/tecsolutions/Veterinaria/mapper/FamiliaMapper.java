package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.FamiliaDTO;
import com.tecsolutions.Veterinaria.model.Familia;
import org.springframework.stereotype.Component;

@Component
public class FamiliaMapper {

    public FamiliaDTO toDTO(Familia entity) {
        if (entity == null) {
            return null;
        }
        return FamiliaDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .build();
    }

    public Familia toEntity(FamiliaDTO dto) {
        if (dto == null) {
            return null;
        }
        Familia entity = new Familia();
        entity.setId(dto.getId());
        entity.setNombre(dto.getNombre());
        return entity;
    }
}
