package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    List<Mascota> findByPropietarioId(Long propietarioId);

    List<Mascota> findByEspecie(String especie);
}
