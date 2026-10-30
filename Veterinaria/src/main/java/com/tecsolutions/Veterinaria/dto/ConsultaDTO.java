package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultaDTO {

    private Long id;

    private LocalDateTime fecha;

    private String diagnostico;

    private String tratamiento;

    private Double peso;

    @JsonProperty("cita_id")
    private Long citaId;

    @JsonProperty("cita_motivo")
    private String citaMotivo;

    @JsonProperty("mascota_id")
    private Long mascotaId;

    @JsonProperty("veterinario_id")
    private Long veterinarioId;

    @Builder.Default
    private List<RecetaMedicaDTO> recetas = new ArrayList<>();

    @Builder.Default
    private List<OrdenExamenDTO> examenes = new ArrayList<>();

    @Builder.Default
    private List<ReferenciaMedicaDTO> referencias = new ArrayList<>();
}
