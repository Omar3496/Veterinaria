package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.OrdenExamenDTO;
import com.tecsolutions.Veterinaria.model.OrdenExamen;
import org.springframework.stereotype.Component;

@Component
public class OrdenExamenMapper {

    public OrdenExamenDTO toDTO(OrdenExamen o) {
        if (o == null) {
            return null;
        }
        String mascotaNombre = null;
        if (o.getConsulta() != null && o.getConsulta().getMascota() != null) {
            mascotaNombre = o.getConsulta().getMascota().getNombre();
        } else if (o.getCita() != null && o.getCita().getMascota() != null) {
            mascotaNombre = o.getCita().getMascota().getNombre();
        }
        return OrdenExamenDTO.builder()
                .id(o.getId())
                .consultaId(o.getConsulta() != null ? o.getConsulta().getId() : null)
                .citaId(o.getCita() != null ? o.getCita().getId() : null)
                .examenAuxiliarId(o.getExamenAuxiliar() != null ? o.getExamenAuxiliar().getId() : null)
                .resultado(o.getResultado())
                .estado(o.getEstado() != null ? o.getEstado() : "PENDIENTE")
                .observaciones(o.getObservaciones())
                .fecha(o.getFecha())
                .examenNombre(o.getExamenNombre())
                .mascotaNombre(mascotaNombre)
                .build();
    }
}
