package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.NotaCredito;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaCreditoRepository extends JpaRepository<NotaCredito, Long> {
    List<NotaCredito> findAllByOrderByFechaEmisionDesc(Pageable pageable);
    List<NotaCredito> findByClienteIdOrderByFechaEmisionDesc(Long clienteId, Pageable pageable);
    boolean existsByCodigo(String codigo);
}
