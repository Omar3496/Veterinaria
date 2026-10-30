package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.RecetaMedicaDTO;
import com.tecsolutions.Veterinaria.model.RecetaMedica;
import org.springframework.stereotype.Component;

@Component
public class RecetaMedicaMapper {

    public RecetaMedicaDTO toDTO(RecetaMedica r) {
        if (r == null) {
            return null;
        }
        String mascotaNombre = null;
        if (r.getConsulta() != null && r.getConsulta().getMascota() != null) {
            mascotaNombre = r.getConsulta().getMascota().getNombre();
        }
        return RecetaMedicaDTO.builder()
                .id(r.getId())
                .consultaId(r.getConsulta() != null ? r.getConsulta().getId() : null)
                .nombreMedicamento(r.getNombreMedicamento())
                .dosis(r.getDosis())
                .cantidad(r.getCantidad())
                .indicaciones(r.getIndicaciones())
                .productoId(r.getProducto() != null ? r.getProducto().getId() : null)
                .productoNombre(r.getProductoNombre())
                .fecha(r.getFecha())
                .mascotaNombre(mascotaNombre)
                .build();
    }
}
