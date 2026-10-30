package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.MovimientoStock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findAllByOrderByFechaDesc(Pageable pageable);
}
