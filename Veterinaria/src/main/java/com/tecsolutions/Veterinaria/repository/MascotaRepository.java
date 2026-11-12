package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Mascota;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    List<Mascota> findByPropietarioId(Long clienteId);
    List<Mascota> findByPropietarioUsuarioId(Long usuarioId, Pageable pageable);
    Optional<Mascota> findByIdAndPropietarioId(Long id, Long clienteId);

    @Query("SELECT COUNT(m) FROM Mascota m WHERE LOWER(m.especie) = LOWER(:especie)")
    Long contarPorEspecie(@Param("especie") String especie);

    @Query("SELECT m FROM Mascota m WHERE m.fechaNacimiento = (SELECT MIN(m2.fechaNacimiento) FROM Mascota m2 WHERE m2.fechaNacimiento IS NOT NULL)")
    List<Mascota> obtenerMascotaMasLongeva();
}
