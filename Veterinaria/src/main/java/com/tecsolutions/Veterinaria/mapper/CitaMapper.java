package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.CitaConMascotaDTO;
import com.tecsolutions.Veterinaria.dto.CitaDTO;
import com.tecsolutions.Veterinaria.model.Cita;
import org.springframework.stereotype.Component;

@Component
public class CitaMapper {

    public CitaDTO toDTO(Cita entity) {
        if (entity == null) {
            return null;
        }
        return CitaDTO.builder()
                .id(entity.getId())
                .fechaHora(entity.getFechaHora())
                .motivo(entity.getMotivo())
                .tipo(entity.getTipo() != null ? entity.getTipo() : "Consulta médica")
                .examenAuxiliarId(entity.getExamenAuxiliar() != null ? entity.getExamenAuxiliar().getId() : null)
                .estado(entity.getEstado())
                .motivoCancelacion(entity.getMotivoCancelacion())
                .mascotaId(entity.getMascota() != null ? entity.getMascota().getId() : null)
                .veterinarioId(entity.getVeterinario() != null ? entity.getVeterinario().getId() : null)
                .veterinarioNombre(entity.getVeterinarioNombre())
                .examenAuxiliarNombre(entity.getExamenAuxiliarNombre())
                .build();
    }

    public CitaConMascotaDTO toConMascotaDTO(Cita entity) {
        if (entity == null) {
            return null;
        }
        return CitaConMascotaDTO.builder()
                .id(entity.getId())
                .fechaHora(entity.getFechaHora())
                .motivo(entity.getMotivo())
                .tipo(entity.getTipo() != null ? entity.getTipo() : "Consulta médica")
                .examenAuxiliarId(entity.getExamenAuxiliar() != null ? entity.getExamenAuxiliar().getId() : null)
                .examenAuxiliarNombre(entity.getExamenAuxiliarNombre())
                .estado(entity.getEstado())
                .motivoCancelacion(entity.getMotivoCancelacion())
                .mascotaId(entity.getMascota() != null ? entity.getMascota().getId() : null)
                .mascotaNombre(entity.getMascota() != null ? entity.getMascota().getNombre() : null)
                .veterinarioId(entity.getVeterinario() != null ? entity.getVeterinario().getId() : null)
                .veterinarioNombre(entity.getVeterinarioNombre())
                .build();
    }
}
