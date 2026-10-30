package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponseDTO {

    @JsonProperty("citas_hoy")
    private Long citasHoy;

    @JsonProperty("vacunas_pendientes")
    private Long vacunasPendientes;

    @JsonProperty("alertas_stock")
    private Long alertasStock;
}
