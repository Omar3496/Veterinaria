package com.tecsolutions.Veterinaria.repository;

import com.tecsolutions.Veterinaria.model.ComprobanteSeq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComprobanteSeqRepository extends JpaRepository<ComprobanteSeq, Long> {
    Optional<ComprobanteSeq> findBySerie(String serie);
}
