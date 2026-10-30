package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Devolucion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {
    List<Devolucion> findAllByOrderByFechaDesc(Pageable pageable);
}
