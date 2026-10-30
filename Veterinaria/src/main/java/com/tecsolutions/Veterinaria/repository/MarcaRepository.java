package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Long> {
    List<Marca> findAllByOrderByNombreAsc();
    Optional<Marca> findByNombre(String nombre);
    Optional<Marca> findByNombreAndIdNot(String nombre, Long id);
}
