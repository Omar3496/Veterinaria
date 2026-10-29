package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MascotaDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String nombre;
    private String especie;
    private String raza;
    private int edad;
    private Long propietarioId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String propietarioNombre;
}
