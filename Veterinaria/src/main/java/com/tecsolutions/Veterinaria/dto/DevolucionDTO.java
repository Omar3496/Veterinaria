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
public class DevolucionDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Long ventaId;

    private Long clienteId;

    private String tipo;

    @Builder.Default
    private Double monto = 0.0;

    private String motivo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fecha;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long usuarioId;

    private Long notaCreditoId;
}
