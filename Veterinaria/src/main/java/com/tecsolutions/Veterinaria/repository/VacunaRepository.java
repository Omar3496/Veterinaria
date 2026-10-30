package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Vacuna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VacunaRepository extends JpaRepository<Vacuna, Long> {
    List<Vacuna> findByMascotaIdOrderByFechaAplicacionDesc(Long mascotaId);
    List<Vacuna> findByProximaDosisNotNullAndProximaDosisLessThanEqual(LocalDate limite);
    List<Vacuna> findByMascotaPropietarioUsuarioIdAndProximaDosisNotNullAndProximaDosisLessThanEqual(Long usuarioId, LocalDate limite);
}
