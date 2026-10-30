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
public class ReferenciaMedicaDTO {

    private Long id;

    @JsonProperty("consulta_id")
    private Long consultaId;

    @JsonProperty("centro_destino")
    private String centroDestino;

    private String especialidad;

    private String motivo;

    @Builder.Default
    private String estado = "ACTIVA";

    private LocalDateTime fecha;

    @JsonProperty("mascota_nombre")
    private String mascotaNombre;
}
