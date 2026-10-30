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
public class VentaCreateDTO {

    private Long clienteId;

    private String metodoPago;

    @Builder.Default
    private String tipoComprobante = "Boleta";

    private String documentoFiscal;

    private Long notaCreditoId;

    @Builder.Default
    private List<DetalleVentaCreateDTO> detalles = new ArrayList<>();
}
