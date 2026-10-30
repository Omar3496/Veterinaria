package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "consultas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha = LocalDateTime.now();

    private String diagnostico;

    private String tratamiento;

    private Double peso;

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario veterinario;

    @ManyToOne
    @JoinColumn(name = "mascota_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Mascota mascota;

    @ManyToOne
    @JoinColumn(name = "cita_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cita cita;

    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RecetaMedica> recetas = new ArrayList<>();

    @OneToMany(mappedBy = "consulta")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrdenExamen> examenes = new ArrayList<>();

    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ReferenciaMedica> referencias = new ArrayList<>();

    public String getCitaMotivo() {
        return cita != null ? cita.getMotivo() : null;
    }
}
