package com.tecsolutions.Veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProximaCitaDTO {

    private Long id;
    private String hora;
    private String mascota;
    private String propietario;
    private String motivo;
    private String estado;
    private String veterinario;
}
