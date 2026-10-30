package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioConfigDTO {

    @Builder.Default
    private List<Integer> dias = List.of(1, 2, 3, 4, 5, 6);

    @JsonProperty("hora_inicio")
    @Builder.Default
    private String horaInicio = "09:00";

    @JsonProperty("hora_fin")
    @Builder.Default
    private String horaFin = "18:00";

    @JsonProperty("duracion_min")
    @Builder.Default
    private Integer duracionMin = 30;
}
