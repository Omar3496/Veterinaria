package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.StockAlmacen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockAlmacenRepository extends JpaRepository<StockAlmacen, Long> {
    List<StockAlmacen> findByProductoId(Long productoId);
    Optional<StockAlmacen> findByProductoIdAndAlmacen(Long productoId, String almacen);
    boolean existsByProductoId(Long productoId);
}
