package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findBySku(String sku);
    boolean existsBySku(String sku);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.stock <= p.stockMinimo")
    long countLowStock();

    @Query("SELECT COALESCE(SUM(p.stock), 0) FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Integer obtenerStockPorMedicamento(@Param("nombre") String nombre);

    @Query("SELECT p FROM Producto p WHERE p.stock <= :stockCritico")
    List<Producto> obtenerMedicamentosCriticos(@Param("stockCritico") Integer stockCritico);

    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND p.stock <= :cantidad")
    List<Producto> buscarPorNombreYStock(@Param("nombre") String nombre, @Param("cantidad") Integer cantidad);
}
