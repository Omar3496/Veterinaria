package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "referencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferenciaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "consulta_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Consulta consulta;

    @Column(name = "centro_destino")
    private String centroDestino;

    private String especialidad;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    private String estado = "ACTIVA";

    private LocalDateTime fecha = LocalDateTime.now();
}
