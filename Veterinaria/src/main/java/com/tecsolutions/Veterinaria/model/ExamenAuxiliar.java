package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "examenes_auxiliares", indexes = {
    @Index(name = "idx_examenes_auxiliares_nombre", columnList = "nombre")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamenAuxiliar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Double precio = 0.0;

    @Column(name = "tiempo_estimado")
    private String tiempoEstimado;

    private Boolean activo = true;
}
