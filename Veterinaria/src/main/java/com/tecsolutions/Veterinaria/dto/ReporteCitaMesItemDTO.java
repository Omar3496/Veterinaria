package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteCitaMesItemDTO {

    private String mes;

    @JsonProperty("Completada")
    private Long completada = 0L;

    @JsonProperty("Cancelada")
    private Long cancelada = 0L;

    @JsonProperty("Programada")
    private Long programada = 0L;

    @JsonProperty("cantidad")
    public Long getCantidad() {
        return (completada != null ? completada : 0L) +
               (cancelada != null ? cancelada : 0L) +
               (programada != null ? programada : 0L);
    }
}
