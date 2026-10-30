package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferenciaMedicaCreateDTO {

    @JsonProperty("centro_destino")
    private String centroDestino;

    private String especialidad;

    private String motivo;
}
