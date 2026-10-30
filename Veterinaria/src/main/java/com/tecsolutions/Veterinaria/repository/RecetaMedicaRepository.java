package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.RecetaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface RecetaMedicaRepository extends JpaRepository<RecetaMedica, Long> {
    List<RecetaMedica> findByConsultaIdInOrderByFechaDesc(Collection<Long> consultaIds);
}
