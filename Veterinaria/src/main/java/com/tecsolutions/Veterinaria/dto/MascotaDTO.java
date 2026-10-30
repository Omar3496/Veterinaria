package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class MascotaDTO {

    private Long id;

    private String nombre;

    private String especie;

    private String raza;

    @JsonProperty("fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @JsonProperty("foto_url")
    private String fotoUrl;

    @JsonProperty("activo")
    @Builder.Default
    private Boolean activo = true;

    @JsonProperty("cliente_id")
    private Long clienteId;

    @Builder.Default
    private List<Object> vacunas = new ArrayList<>();

    @Builder.Default
    private List<Object> consultas = new ArrayList<>();
}
