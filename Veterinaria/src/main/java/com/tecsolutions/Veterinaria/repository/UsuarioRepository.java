package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
}
