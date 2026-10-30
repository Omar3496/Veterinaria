package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByOrderByNombreAsc();
    Optional<Categoria> findByNombre(String nombre);
    Optional<Categoria> findByNombreAndIdNot(String nombre, Long id);
}
