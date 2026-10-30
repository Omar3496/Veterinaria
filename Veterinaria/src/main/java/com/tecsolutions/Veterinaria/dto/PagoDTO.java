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
public class PagoDTO {

    private Long id;

    @JsonProperty("cita_id")
    private Long citaId;

    private Double monto;

    private String metodo;

    private String estado;

    private String referencia;

    private String detalle;

    private LocalDateTime fecha;

    @JsonProperty("tipo_comprobante")
    private String tipoComprobante;

    @JsonProperty("documento_fiscal")
    private String documentoFiscal;

    private String serie;

    private String numero;

    @JsonProperty(value = "comprobante", access = JsonProperty.Access.READ_ONLY)
    private String comprobante;
}
