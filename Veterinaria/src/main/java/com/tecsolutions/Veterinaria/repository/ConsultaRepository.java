package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findByMascotaIdOrderByFechaDesc(Long mascotaId);
    List<Consulta> findByMascotaIdIn(Collection<Long> mascotaIds);
    Optional<Consulta> findFirstByCitaId(Long citaId);
}
