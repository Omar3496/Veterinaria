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
public class MovimientoStockDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Long productoId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String productoNombre;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fecha;

    private String tipoMovimiento;

    private Integer cantidad;

    private String estadoAnterior;

    private String estadoNuevo;

    private String observacion;

    private String referencia;

    private Long usuarioId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String usuarioNombre;
}
