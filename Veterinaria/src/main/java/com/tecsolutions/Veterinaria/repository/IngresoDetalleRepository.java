package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.IngresoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngresoDetalleRepository extends JpaRepository<IngresoDetalle, Long> {
    List<IngresoDetalle> findByIngresoId(Long ingresoId);
}
