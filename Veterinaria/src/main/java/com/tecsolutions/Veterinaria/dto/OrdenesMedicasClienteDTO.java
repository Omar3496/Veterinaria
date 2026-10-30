package com.tecsolutions.Veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenesMedicasClienteDTO {

    @Builder.Default
    private List<RecetaMedicaDTO> recetas = new ArrayList<>();

    @Builder.Default
    private List<OrdenExamenDTO> examenes = new ArrayList<>();

    @Builder.Default
    private List<ReferenciaMedicaDTO> referencias = new ArrayList<>();
}
