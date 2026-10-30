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
@Table(name = "mascotas", indexes = {
    @Index(name = "idx_mascotas_nombre", columnList = "nombre")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String especie;

    private String raza;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "foto_url", columnDefinition = "TEXT")
    private String fotoUrl;

    private Boolean activo = true;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cliente propietario;

    @OneToMany(mappedBy = "mascota", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Cita> citas = new ArrayList<>();

    @OneToMany(mappedBy = "mascota")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Consulta> consultas = new ArrayList<>();

    @OneToMany(mappedBy = "mascota")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Vacuna> vacunas = new ArrayList<>();
}
