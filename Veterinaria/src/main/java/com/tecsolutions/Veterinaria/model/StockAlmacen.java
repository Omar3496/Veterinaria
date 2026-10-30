package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "productos_stock_almacen", indexes = {
    @Index(name = "idx_stock_almacen_producto_id", columnList = "producto_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockAlmacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Producto producto;

    private String almacen = "Disponible";

    private Integer cantidad = 0;
}
