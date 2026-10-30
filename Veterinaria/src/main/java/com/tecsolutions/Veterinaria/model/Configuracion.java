package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracion", indexes = {
    @Index(name = "idx_configuracion_clave", columnList = "clave")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Configuracion {

    @Id
    private String clave;

    @Column(columnDefinition = "TEXT")
    private String valor;
}
