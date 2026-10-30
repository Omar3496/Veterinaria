package com.tecsolutions.Veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoAlmacenRequestDTO {

    private Long productoId;

    private Integer cantidad;

    private String almacenOrigen;

    private String almacenDestino;

    private String observacion;
}
