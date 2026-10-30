package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_productos_sku", columnList = "sku")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String sku;

    @Column(name = "codigo_barra")
    private String codigoBarra;

    @Column(columnDefinition = "TEXT")
    private String imagen;

    private String nombre;

    private String descripcion;

    private Double precio;

    private Integer stock = 0;

    @Column(name = "stock_minimo")
    private Integer stockMinimo = 5;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "es_perecible")
    private Boolean esPerecible = false;

    @Column(name = "precio_compra")
    private Double precioCompra = 0.0;

    @Column(name = "estado_almacen")
    @Convert(converter = EstadoAlmacenConverter.class)
    private EstadoAlmacen estadoAlmacen = EstadoAlmacen.DISPONIBLE;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "familia_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Familia familia;

    @ManyToOne
    @JoinColumn(name = "marca_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Marca marca;

    @OneToMany(mappedBy = "producto")
    @OrderBy("fecha DESC")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MovimientoStock> movimientos = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<StockAlmacen> stocksAlmacen = new ArrayList<>();

    public String getProveedorNombre() {
        return proveedor != null ? proveedor.getRazonSocial() : null;
    }

    public String getCategoriaNombre() {
        return categoria != null ? categoria.getNombre() : null;
    }

    public String getFamiliaNombre() {
        return familia != null ? familia.getNombre() : null;
    }

    public String getMarcaNombre() {
        return marca != null ? marca.getNombre() : null;
    }

    public String getEstadoAlmacenNombre() {
        return estadoAlmacen != null ? estadoAlmacen.getValue() : null;
    }
}
