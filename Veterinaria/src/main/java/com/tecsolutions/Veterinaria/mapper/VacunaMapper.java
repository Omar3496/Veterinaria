package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.VacunaDTO;
import com.tecsolutions.Veterinaria.model.Vacuna;
import org.springframework.stereotype.Component;

@Component
public class VacunaMapper {

    public VacunaDTO toDTO(Vacuna v) {
        if (v == null) {
            return null;
        }
        return VacunaDTO.builder()
                .id(v.getId())
                .tipo(v.getTipo())
                .fechaAplicacion(v.getFechaAplicacion())
                .proximaDosis(v.getProximaDosis())
                .productoId(v.getProducto() != null ? v.getProducto().getId() : null)
                .productoNombre(v.getProductoNombre())
                .intervaloDias(v.getIntervaloDias())
                .mascotaId(v.getMascota() != null ? v.getMascota().getId() : null)
                .veterinarioId(v.getVeterinario() != null ? v.getVeterinario().getId() : null)
                .build();
    }
}
