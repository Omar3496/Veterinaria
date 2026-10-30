package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);
    Optional<Cliente> findByTelefono(String telefono);
    Optional<Cliente> findByUsuarioId(Long usuarioId);
    Optional<Cliente> findByEmailIgnoreCase(String email);
    boolean existsByDniOrTelefono(String dni, String telefono);
}
