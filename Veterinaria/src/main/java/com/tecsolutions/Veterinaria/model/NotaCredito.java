package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "notas_credito", indexes = {
    @Index(name = "idx_notas_credito_codigo", columnList = "codigo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String codigo;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cliente cliente;

    @Column(name = "venta_id")
    private Long ventaId;

    private Double monto = 0.0;

    private Double saldo = 0.0;

    @Column(name = "fecha_emision")
    private LocalDateTime fechaEmision = LocalDateTime.now();

    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;

    private String estado = "ACTIVA";

    private String motivo;

    public String getClienteNombre() {
        return cliente != null ? cliente.getNombre() : null;
    }

    public boolean isVigente() {
        if (!"ACTIVA".equals(estado)) {
            return false;
        }
        if (fechaVencimiento != null && fechaVencimiento.isBefore(LocalDateTime.now())) {
            return false;
        }
        return saldo != null && saldo > 0;
    }
}
