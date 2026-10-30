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
public class IngresoDetalleDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Long productoId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String productoNombre;

    private Integer cantidad;

    private Double precioUnitario;

    private Double precioVenta;

    private LocalDate fechaVencimiento;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Double subtotal;
}
