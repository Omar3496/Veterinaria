package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VentaDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fecha;

    private Long clienteId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String clienteNombre;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long usuarioId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String vendedorNombre;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Double total;

    private String metodoPago;

    @Builder.Default
    private String tipoComprobante = "Boleta";

    private String documentoFiscal;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String serie;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String numero;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String comprobante;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String codigoRecojo;

    @Builder.Default
    private String estado = "Pendiente";

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String estadoNombre;

    private Long notaCreditoId;

    @Builder.Default
    private Double montoNota = 0.0;

    @Builder.Default
    private List<DetalleVentaDTO> detalles = new ArrayList<>();
}
