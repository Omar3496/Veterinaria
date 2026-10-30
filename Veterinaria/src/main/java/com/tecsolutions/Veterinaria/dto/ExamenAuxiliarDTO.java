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
public class ExamenAuxiliarDTO {

    private Long id;

    private String nombre;

    private String descripcion;

    @Builder.Default
    private Double precio = 0.0;

    @JsonProperty("tiempo_estimado")
    private String tiempoEstimado;

    @Builder.Default
    private Boolean activo = true;
}
