package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Producto producto;

    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "tipo_movimiento")
    private String tipoMovimiento;

    private Integer cantidad = 0;

    @Column(name = "estado_anterior")
    private String estadoAnterior;

    @Column(name = "estado_nuevo")
    private String estadoNuevo;

    private String observacion;

    private String referencia;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    public String getProductoNombre() {
        return producto != null ? producto.getNombre() : null;
    }

    public String getUsuarioNombre() {
        return usuario != null ? usuario.getUsername() : null;
    }
}
