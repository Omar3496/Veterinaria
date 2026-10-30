package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.ConsultaDTO;
import com.tecsolutions.Veterinaria.dto.OrdenExamenDTO;
import com.tecsolutions.Veterinaria.dto.RecetaMedicaDTO;
import com.tecsolutions.Veterinaria.dto.ReferenciaMedicaDTO;
import com.tecsolutions.Veterinaria.model.Consulta;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ConsultaMapper {

    private final RecetaMedicaMapper recetaMedicaMapper;
    private final OrdenExamenMapper ordenExamenMapper;
    private final ReferenciaMedicaMapper referenciaMedicaMapper;

    public ConsultaMapper(
            RecetaMedicaMapper recetaMedicaMapper,
            OrdenExamenMapper ordenExamenMapper,
            ReferenciaMedicaMapper referenciaMedicaMapper) {
        this.recetaMedicaMapper = recetaMedicaMapper;
        this.ordenExamenMapper = ordenExamenMapper;
        this.referenciaMedicaMapper = referenciaMedicaMapper;
    }

    public ConsultaDTO toDTO(Consulta c) {
        if (c == null) {
            return null;
        }

        List<RecetaMedicaDTO> recetasDTO = new ArrayList<>();
        if (c.getRecetas() != null) {
            for (var r : c.getRecetas()) {
                recetasDTO.add(recetaMedicaMapper.toDTO(r));
            }
        }

        List<OrdenExamenDTO> examenesDTO = new ArrayList<>();
        if (c.getExamenes() != null) {
            for (var ex : c.getExamenes()) {
                examenesDTO.add(ordenExamenMapper.toDTO(ex));
            }
        }

        List<ReferenciaMedicaDTO> referenciasDTO = new ArrayList<>();
        if (c.getReferencias() != null) {
            for (var ref : c.getReferencias()) {
                referenciasDTO.add(referenciaMedicaMapper.toDTO(ref));
            }
        }

        return ConsultaDTO.builder()
                .id(c.getId())
                .fecha(c.getFecha())
                .diagnostico(c.getDiagnostico())
                .tratamiento(c.getTratamiento())
                .peso(c.getPeso())
                .citaId(c.getCita() != null ? c.getCita().getId() : null)
                .citaMotivo(c.getCitaMotivo())
                .mascotaId(c.getMascota() != null ? c.getMascota().getId() : null)
                .veterinarioId(c.getVeterinario() != null ? c.getVeterinario().getId() : null)
                .recetas(recetasDTO)
                .examenes(examenesDTO)
                .referencias(referenciasDTO)
                .build();
    }
}
