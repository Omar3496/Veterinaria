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
public class HistorialMascotaDTO {

    @Builder.Default
    private List<VacunaDTO> vacunas = new ArrayList<>();

    @Builder.Default
    private List<ConsultaDTO> consultas = new ArrayList<>();

    @Builder.Default
    private List<RecetaMedicaDTO> recetas = new ArrayList<>();

    @Builder.Default
    private List<OrdenExamenDTO> examenes = new ArrayList<>();

    @Builder.Default
    private List<ReferenciaMedicaDTO> referencias = new ArrayList<>();
}
