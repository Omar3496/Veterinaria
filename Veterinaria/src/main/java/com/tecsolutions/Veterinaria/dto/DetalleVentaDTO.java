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
public class DetalleVentaDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Long productoId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String productoNombre;

    private Integer cantidad;

    private Double precioUnitario;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Double subtotal;
}
