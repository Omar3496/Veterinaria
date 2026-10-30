package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecetaMedicaCreateDTO {

    @JsonProperty("nombre_medicamento")
    private String nombreMedicamento;

    private String dosis;

    private String cantidad;

    private String indicaciones;

    @JsonProperty("producto_id")
    private Long productoId;
}
