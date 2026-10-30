package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "ingresos_detalle")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngresoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ingreso_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Ingreso ingreso;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Producto producto;

    private Integer cantidad;

    @Column(name = "precio_unitario")
    private Double precioUnitario;

    @Column(name = "precio_venta")
    private Double precioVenta;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    private Double subtotal;

    public String getProductoNombre() {
        return producto != null ? producto.getNombre() : null;
    }
}
