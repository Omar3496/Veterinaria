package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "recetas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecetaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "consulta_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Consulta consulta;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Producto producto;

    @Column(name = "nombre_medicamento")
    private String nombreMedicamento;

    private String dosis;

    private String cantidad;

    @Column(columnDefinition = "TEXT")
    private String indicaciones;

    private LocalDateTime fecha = LocalDateTime.now();

    public String getProductoNombre() {
        return producto != null ? producto.getNombre() : null;
    }
}
