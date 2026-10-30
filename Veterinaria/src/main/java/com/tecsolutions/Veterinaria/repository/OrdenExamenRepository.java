package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.OrdenExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrdenExamenRepository extends JpaRepository<OrdenExamen, Long> {
    List<OrdenExamen> findByConsultaMascotaIdOrderByFechaDesc(Long mascotaId);
    List<OrdenExamen> findByConsultaIdIn(Collection<Long> consultaIds);
    List<OrdenExamen> findByCitaMascotaIdIn(Collection<Long> mascotaIds);
    boolean existsByExamenAuxiliarId(Long examenAuxiliarId);
}
