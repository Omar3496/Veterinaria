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
public class IngresoDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime fecha;

    private Long proveedorId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String proveedorNombre;

    @Builder.Default
    private String tipoPago = "Contado";

    @Builder.Default
    private String tipoComprobante = "Factura";

    private String serie;

    private String numero;

    private String numeroFactura;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Double total;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long usuarioId;

    @Builder.Default
    private List<IngresoDetalleDTO> detalles = new ArrayList<>();
}
