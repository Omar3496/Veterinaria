package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitaDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private LocalDateTime fechaHora;
    private String motivo;
    private String estado;
    private String observaciones;
    private Long mascotaId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String mascotaNombre;

    private Long veterinarioId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String veterinarioNombre;
}
