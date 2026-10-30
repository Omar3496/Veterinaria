package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    private String motivo;

    private String tipo = "Consulta médica";

    @Convert(converter = EstadoCitaConverter.class)
    private EstadoCita estado = EstadoCita.PROGRAMADA;

    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;

    @ManyToOne
    @JoinColumn(name = "mascota_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Mascota mascota;

    @ManyToOne
    @JoinColumn(name = "veterinario_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario veterinario;

    @ManyToOne
    @JoinColumn(name = "examen_auxiliar_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ExamenAuxiliar examenAuxiliar;

    @OneToOne(mappedBy = "cita")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdenExamen ordenExamen;

    public String getVeterinarioNombre() {
        return veterinario != null ? veterinario.getUsername() : null;
    }

    public String getExamenAuxiliarNombre() {
        return examenAuxiliar != null ? examenAuxiliar.getNombre() : null;
    }
}
