package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proveedores", indexes = {
    @Index(name = "idx_proveedores_ruc", columnList = "ruc")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String ruc;

    @Column(name = "razon_social")
    private String razonSocial;

    private String contacto;

    private String telefono;

    private String direccion;

    private String email;

    private String moneda = "SOL";

    @OneToMany(mappedBy = "proveedor")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Producto> productos = new ArrayList<>();

    @OneToMany(mappedBy = "proveedor")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Ingreso> ingresos = new ArrayList<>();
}
