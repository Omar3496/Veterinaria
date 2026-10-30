package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaDTO {

    private Long id;

    @JsonProperty("usuario_id")
    private Long usuarioId;

    private String accion;

    private String endpoint;

    @JsonProperty("resource_id")
    private String resourceId;

    private LocalDateTime timestamp;

    private String detalle;
}
