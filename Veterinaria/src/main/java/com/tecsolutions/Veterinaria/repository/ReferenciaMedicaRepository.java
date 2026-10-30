package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.ReferenciaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ReferenciaMedicaRepository extends JpaRepository<ReferenciaMedica, Long> {
    List<ReferenciaMedica> findByConsultaIdInOrderByFechaDesc(Collection<Long> consultaIds);
}
