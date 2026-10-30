package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.ReferenciaMedicaDTO;
import com.tecsolutions.Veterinaria.model.ReferenciaMedica;
import org.springframework.stereotype.Component;

@Component
public class ReferenciaMedicaMapper {

    public ReferenciaMedicaDTO toDTO(ReferenciaMedica ref) {
        if (ref == null) {
            return null;
        }
        String mascotaNombre = null;
        if (ref.getConsulta() != null && ref.getConsulta().getMascota() != null) {
            mascotaNombre = ref.getConsulta().getMascota().getNombre();
        }
        return ReferenciaMedicaDTO.builder()
                .id(ref.getId())
                .consultaId(ref.getConsulta() != null ? ref.getConsulta().getId() : null)
                .centroDestino(ref.getCentroDestino())
                .especialidad(ref.getEspecialidad())
                .motivo(ref.getMotivo())
                .estado(ref.getEstado() != null ? ref.getEstado() : "ACTIVA")
                .fecha(ref.getFecha())
                .mascotaNombre(mascotaNombre)
                .build();
    }
}
