package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comprobantes_seq", indexes = {
    @Index(name = "idx_comprobantes_seq_serie", columnList = "serie")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteSeq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String serie;

    @Column(name = "ultimo_numero")
    private Integer ultimoNumero = 0;
}
