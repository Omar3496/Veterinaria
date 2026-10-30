package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoCreateDTO {

    private String metodo;

    private Double monto;

    @JsonProperty("tipo_comprobante")
    @Builder.Default
    private String tipoComprobante = "Boleta";

    @JsonProperty("documento_fiscal")
    private String documentoFiscal;
}
