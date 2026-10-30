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
public class NotaCreditoDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String codigo;

    private Long clienteId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String clienteNombre;

    private Long ventaId;

    @Builder.Default
    private Double monto = 0.0;

    @Builder.Default
    private Double saldo = 0.0;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fechaEmision;

    private LocalDateTime fechaVencimiento;

    @Builder.Default
    private String estado = "ACTIVA";

    private String motivo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean vigente;
}
