package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByCitaIdInOrderByFechaDesc(Collection<Long> citaIds);
    List<Pago> findBySerie(String serie);
}
