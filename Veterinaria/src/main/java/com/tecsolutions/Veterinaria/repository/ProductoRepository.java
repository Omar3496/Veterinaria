package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findBySku(String sku);
    boolean existsBySku(String sku);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stock <= p.stockMinimo")
    long countLowStock();
}
