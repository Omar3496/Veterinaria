package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.MascotaDTO;
import com.tecsolutions.Veterinaria.model.Mascota;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class MascotaMapper {

    public MascotaDTO toDTO(Mascota mascota) {
        if (mascota == null) {
            return null;
        }
        return MascotaDTO.builder()
                .id(mascota.getId())
                .nombre(mascota.getNombre())
                .especie(mascota.getEspecie())
                .raza(mascota.getRaza())
                .fechaNacimiento(mascota.getFechaNacimiento())
                .fotoUrl(mascota.getFotoUrl())
                .activo(mascota.getActivo() != null ? mascota.getActivo() : true)
                .clienteId(mascota.getPropietario() != null ? mascota.getPropietario().getId() : null)
                .vacunas(new ArrayList<>())
                .consultas(new ArrayList<>())
                .build();
    }

    public Mascota toEntity(MascotaDTO dto) {
        if (dto == null) {
            return null;
        }
        Mascota mascota = new Mascota();
        mascota.setId(dto.getId());
        mascota.setNombre(dto.getNombre());
        mascota.setEspecie(dto.getEspecie());
        mascota.setRaza(dto.getRaza());
        mascota.setFechaNacimiento(dto.getFechaNacimiento());
        mascota.setFotoUrl(dto.getFotoUrl());
        mascota.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return mascota;
    }
}
