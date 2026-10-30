package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.ExamenAuxiliar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamenAuxiliarRepository extends JpaRepository<ExamenAuxiliar, Long> {
    List<ExamenAuxiliar> findAllByOrderByNombreAsc();
    Optional<ExamenAuxiliar> findByNombre(String nombre);
    Optional<ExamenAuxiliar> findByNombreAndIdNot(String nombre, Long id);
}
