package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Cita;
import com.tecsolutions.Veterinaria.model.EstadoCita;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    List<Cita> findByMascotaPropietarioIdOrderByFechaHoraDesc(Long clienteId, Pageable pageable);
    List<Cita> findByMascotaPropietarioId(Long clienteId);
    List<Cita> findByFechaHoraBetweenAndEstadoNot(LocalDateTime inicio, LocalDateTime fin, EstadoCita estado);
    List<Cita> findByVeterinarioIdAndFechaHoraBetweenAndEstadoNot(Long veterinarioId, LocalDateTime inicio, LocalDateTime fin, EstadoCita estado);
    long countByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
    long countByFechaHoraGreaterThanEqualAndEstado(LocalDateTime fecha, EstadoCita estado);
}
