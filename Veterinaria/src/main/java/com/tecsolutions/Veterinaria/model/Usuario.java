package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_usuarios_username", columnList = "username")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column(name = "hashed_password")
    private String hashedPassword;

    @Convert(converter = RolEnumConverter.class)
    private RolEnum rol = RolEnum.RECEPCIONISTA;

    @Column(name = "is_active")
    private Boolean isActive = true;

    private String email;
}
