package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes_examen")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "consulta_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Consulta consulta;

    @OneToOne
    @JoinColumn(name = "cita_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cita cita;

    @ManyToOne
    @JoinColumn(name = "examen_auxiliar_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ExamenAuxiliar examenAuxiliar;

    @Column(columnDefinition = "TEXT")
    private String resultado;

    private String estado = "PENDIENTE";

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    private LocalDateTime fecha = LocalDateTime.now();

    public String getExamenNombre() {
        return examenAuxiliar != null ? examenAuxiliar.getNombre() : null;
    }
}
