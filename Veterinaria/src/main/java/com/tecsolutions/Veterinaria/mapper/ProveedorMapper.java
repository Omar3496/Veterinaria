package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.ProveedorDTO;
import com.tecsolutions.Veterinaria.model.Proveedor;
import org.springframework.stereotype.Component;

@Component
public class ProveedorMapper {

    public ProveedorDTO toDTO(Proveedor entity) {
        if (entity == null) {
            return null;
        }
        return ProveedorDTO.builder()
                .id(entity.getId())
                .ruc(entity.getRuc())
                .razonSocial(entity.getRazonSocial())
                .contacto(entity.getContacto())
                .telefono(entity.getTelefono())
                .direccion(entity.getDireccion())
                .email(entity.getEmail())
                .moneda(entity.getMoneda() != null ? entity.getMoneda() : "SOL")
                .build();
    }

    public Proveedor toEntity(ProveedorDTO dto) {
        if (dto == null) {
            return null;
        }
        Proveedor entity = new Proveedor();
        entity.setId(dto.getId());
        entity.setRuc(dto.getRuc());
        entity.setRazonSocial(dto.getRazonSocial());
        entity.setContacto(dto.getContacto());
        entity.setTelefono(dto.getTelefono());
        entity.setDireccion(dto.getDireccion());
        entity.setEmail(dto.getEmail());
        entity.setMoneda(dto.getMoneda() != null ? dto.getMoneda() : "SOL");
        return entity;
    }
}
