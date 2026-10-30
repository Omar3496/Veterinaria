package com.tecsolutions.Veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteProductoItemDTO {

    private String nombre;
    private Long cantidad;
}
