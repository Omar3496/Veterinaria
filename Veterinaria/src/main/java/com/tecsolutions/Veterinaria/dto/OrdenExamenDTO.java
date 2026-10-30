package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenExamenDTO {

    private Long id;

    @JsonProperty("consulta_id")
    private Long consultaId;

    @JsonProperty("cita_id")
    private Long citaId;

    @JsonProperty("examen_auxiliar_id")
    private Long examenAuxiliarId;

    private String resultado;

    @Builder.Default
    private String estado = "PENDIENTE";

    private String observaciones;

    private LocalDateTime fecha;

    @JsonProperty("examen_nombre")
    private String examenNombre;

    @JsonProperty("mascota_nombre")
    private String mascotaNombre;
}
