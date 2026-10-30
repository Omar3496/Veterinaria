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
@Table(name = "ventas", indexes = {
    @Index(name = "idx_ventas_codigo_recojo", columnList = "codigo_recojo")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Usuario usuario;

    private Double total = 0.0;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @Column(name = "tipo_comprobante")
    private String tipoComprobante = "Boleta";

    @Column(name = "documento_fiscal")
    private String documentoFiscal;

    private String serie;

    private String numero;

    @Column(name = "codigo_recojo")
    private String codigoRecojo;

    @Convert(converter = VentaEstadoConverter.class)
    private VentaEstado estado = VentaEstado.PENDIENTE;

    @Column(name = "nota_credito_id")
    private Long notaCreditoId;

    @Column(name = "monto_nota")
    private Double montoNota = 0.0;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DetalleVenta> detalles = new ArrayList<>();

    public String getClienteNombre() {
        return cliente != null ? cliente.getNombre() : null;
    }

    public String getVendedorNombre() {
        return usuario != null ? usuario.getUsername() : null;
    }

    public String getEstadoNombre() {
        return estado != null ? estado.getValue() : null;
    }

    public String getComprobante() {
        if (serie != null && !serie.isEmpty() && numero != null && !numero.isEmpty()) {
            return serie + "-" + numero;
        }
        return documentoFiscal;
    }
}
