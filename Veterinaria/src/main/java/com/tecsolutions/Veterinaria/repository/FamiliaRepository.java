package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Familia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamiliaRepository extends JpaRepository<Familia, Long> {
    List<Familia> findAllByOrderByNombreAsc();
    Optional<Familia> findByNombre(String nombre);
    Optional<Familia> findByNombreAndIdNot(String nombre, Long id);
}
