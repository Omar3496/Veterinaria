package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByMascotaId(Long mascotaId);

    List<Cita> findByVeterinarioId(Long veterinarioId);

    List<Cita> findByEstado(String estado);
}
