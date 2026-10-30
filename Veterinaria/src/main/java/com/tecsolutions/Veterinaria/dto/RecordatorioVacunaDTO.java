package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordatorioVacunaDTO {

    @JsonProperty("vacuna_id")
    private Long vacunaId;

    @JsonProperty("mascota_id")
    private Long mascotaId;

    private String tipo;

    @JsonProperty("proxima_dosis")
    private LocalDate proximaDosis;

    @JsonProperty("mascota_nombre")
    private String mascotaNombre;

    @JsonProperty("cliente_nombre")
    private String clienteNombre;

    @JsonProperty("cliente_telefono")
    private String clienteTelefono;
}
