package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.ConsultaDTO;
import com.tecsolutions.Veterinaria.dto.MascotaDTO;
import com.tecsolutions.Veterinaria.dto.VacunaDTO;
import com.tecsolutions.Veterinaria.model.Consulta;
import com.tecsolutions.Veterinaria.model.Mascota;
import com.tecsolutions.Veterinaria.model.Vacuna;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MascotaMapper {

    private final VacunaMapper vacunaMapper;
    private final ConsultaMapper consultaMapper;

    public MascotaMapper(VacunaMapper vacunaMapper, ConsultaMapper consultaMapper) {
        this.vacunaMapper = vacunaMapper;
        this.consultaMapper = consultaMapper;
    }

    public MascotaDTO toDTO(Mascota mascota) {
        if (mascota == null) {
            return null;
        }

        List<VacunaDTO> vacunasDTO = new ArrayList<>();
        if (mascota.getVacunas() != null) {
            for (Vacuna v : mascota.getVacunas()) {
                vacunasDTO.add(vacunaMapper.toDTO(v));
            }
        }

        List<ConsultaDTO> consultasDTO = new ArrayList<>();
        if (mascota.getConsultas() != null) {
            for (Consulta c : mascota.getConsultas()) {
                consultasDTO.add(consultaMapper.toDTO(c));
            }
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
                .vacunas(vacunasDTO)
                .consultas(consultasDTO)
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
