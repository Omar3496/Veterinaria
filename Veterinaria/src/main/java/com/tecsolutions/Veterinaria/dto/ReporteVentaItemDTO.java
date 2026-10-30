package com.tecsolutions.Veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentaItemDTO {

    private String fecha;
    private Double total;
}
