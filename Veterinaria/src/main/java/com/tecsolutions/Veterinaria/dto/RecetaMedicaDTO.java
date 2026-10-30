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
public class RecetaMedicaDTO {

    private Long id;

    @JsonProperty("consulta_id")
    private Long consultaId;

    @JsonProperty("nombre_medicamento")
    private String nombreMedicamento;

    private String dosis;

    private String cantidad;

    private String indicaciones;

    @JsonProperty("producto_id")
    private Long productoId;

    @JsonProperty("producto_nombre")
    private String productoNombre;

    private LocalDateTime fecha;

    @JsonProperty("mascota_nombre")
    private String mascotaNombre;
}
