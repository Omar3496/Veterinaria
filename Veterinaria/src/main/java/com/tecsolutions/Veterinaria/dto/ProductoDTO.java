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
public class ProductoDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String sku;

    private String codigoBarra;

    private String nombre;

    private String descripcion;

    private Double precio;

    @Builder.Default
    private Integer stock = 0;

    @Builder.Default
    private Integer stockMinimo = 5;

    private LocalDate fechaVencimiento;

    @Builder.Default
    private Boolean esPerecible = false;

    @Builder.Default
    private Double precioCompra = 0.0;

    @Builder.Default
    private String estadoAlmacen = "Disponible";

    private Long proveedorId;

    private Long categoriaId;

    private Long familiaId;

    private Long marcaId;

    private String imagen;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String proveedorNombre;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String categoriaNombre;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String familiaNombre;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String marcaNombre;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String estadoAlmacenNombre;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Builder.Default
    private List<StockAlmacenItemDTO> stockPorAlmacen = new ArrayList<>();
}
