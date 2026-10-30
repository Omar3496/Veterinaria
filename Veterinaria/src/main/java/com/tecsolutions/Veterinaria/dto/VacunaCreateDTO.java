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
public class VacunaCreateDTO {

    private String tipo;

    @JsonProperty("fecha_aplicacion")
    private LocalDate fechaAplicacion;

    @JsonProperty("proxima_dosis")
    private LocalDate proximaDosis;

    @JsonProperty("producto_id")
    private Long productoId;

    @JsonProperty("intervalo_dias")
    private Integer intervaloDias;
}
