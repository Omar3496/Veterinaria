package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaCreateDTO {

    private String diagnostico;

    private String tratamiento;

    private Double peso;

    @JsonProperty("cita_id")
    private Long citaId;

    @Builder.Default
    private List<RecetaMedicaCreateDTO> recetas = new ArrayList<>();

    @Builder.Default
    private List<OrdenExamenCreateDTO> examenes = new ArrayList<>();

    private ReferenciaMedicaCreateDTO referencia;
}
