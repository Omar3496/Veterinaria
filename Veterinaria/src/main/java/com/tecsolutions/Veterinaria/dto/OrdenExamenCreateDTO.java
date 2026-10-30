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
public class OrdenExamenCreateDTO {

    @JsonProperty("examen_auxiliar_id")
    private Long examenAuxiliarId;

    private String resultado;

    private String observaciones;
}
