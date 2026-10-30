package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "vacunas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vacuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tipo;

    @Column(name = "fecha_aplicacion")
    private LocalDate fechaAplicacion;

    @Column(name = "proxima_dosis")
    private LocalDate proximaDosis;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Producto producto;

    @Column(name = "intervalo_dias")
    private Integer intervaloDias;

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

    public String getProductoNombre() {
        return producto != null ? producto.getNombre() : null;
    }
}
