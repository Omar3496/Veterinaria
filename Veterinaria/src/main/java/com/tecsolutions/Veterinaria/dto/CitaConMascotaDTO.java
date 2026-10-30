package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tecsolutions.Veterinaria.model.EstadoCita;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CitaConMascotaDTO {

    private Long id;

    @JsonProperty("fecha_hora")
    private LocalDateTime fechaHora;

    private String motivo;

    @Builder.Default
    private String tipo = "Consulta médica";

    @JsonProperty("examen_auxiliar_id")
    private Long examenAuxiliarId;

    @JsonProperty("examen_auxiliar_nombre")
    private String examenAuxiliarNombre;

    private EstadoCita estado;

    @JsonProperty("motivo_cancelacion")
    private String motivoCancelacion;

    @JsonProperty("mascota_id")
    private Long mascotaId;

    @JsonProperty("mascota_nombre")
    private String mascotaNombre;

    @JsonProperty("veterinario_id")
    private Long veterinarioId;

    @JsonProperty("veterinario_nombre")
    private String veterinarioNombre;
}
