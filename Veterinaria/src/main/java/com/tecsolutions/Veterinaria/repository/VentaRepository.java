package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Venta;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findAllByOrderByFechaDesc(Pageable pageable);
    List<Venta> findByClienteIdOrderByFechaDesc(Long clienteId);
    boolean existsByCodigoRecojo(String codigoRecojo);
    List<Venta> findBySerie(String serie);
}
