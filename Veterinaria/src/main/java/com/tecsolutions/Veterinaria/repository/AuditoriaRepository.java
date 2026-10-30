package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findTop100ByOrderByTimestampDesc();
}
