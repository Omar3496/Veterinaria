package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.CategoriaDTO;
import com.tecsolutions.Veterinaria.model.Categoria;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public CategoriaDTO toDTO(Categoria entity) {
        if (entity == null) {
            return null;
        }
        return CategoriaDTO.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .build();
    }

    public Categoria toEntity(CategoriaDTO dto) {
        if (dto == null) {
            return null;
        }
        Categoria entity = new Categoria();
        entity.setId(dto.getId());
        entity.setNombre(dto.getNombre());
        return entity;
    }
}
