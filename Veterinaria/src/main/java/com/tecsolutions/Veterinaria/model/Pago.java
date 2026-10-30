package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cita_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cita cita;

    private Double monto = 0.0;

    private String metodo;

    private String estado = "PENDIENTE";

    private String referencia;

    private String detalle;

    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "tipo_comprobante")
    private String tipoComprobante = "Boleta";

    @Column(name = "documento_fiscal")
    private String documentoFiscal;

    private String serie;

    private String numero;

    public String getComprobante() {
        if (serie != null && !serie.isEmpty() && numero != null && !numero.isEmpty()) {
            return serie + "-" + numero;
        }
        return documentoFiscal;
    }
}
