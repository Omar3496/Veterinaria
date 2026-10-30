package com.tecsolutions.Veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadDiaDTO {

    private LocalDate fecha;

    private Integer dia;

    private Boolean disponible;

    @Builder.Default
    private List<HorarioSlotDTO> horarios = new ArrayList<>();
}
