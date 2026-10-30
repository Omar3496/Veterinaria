package com.tecsolutions.Veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioEstadoAlmacenRequestDTO {

    private String estado;

    private String observacion;
}
