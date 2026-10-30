package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Mascota;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByPropietarioId(Long clienteId);
    List<Mascota> findByPropietarioUsuarioId(Long usuarioId, Pageable pageable);
    Optional<Mascota> findByIdAndPropietarioId(Long id, Long clienteId);
}
