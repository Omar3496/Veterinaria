package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.MovimientoStockDTO;
import com.tecsolutions.Veterinaria.mapper.MovimientoStockMapper;
import com.tecsolutions.Veterinaria.repository.MovimientoStockRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final MovimientoStockMapper movimientoStockMapper;

    public MovimientoStockService(
            MovimientoStockRepository movimientoStockRepository,
            MovimientoStockMapper movimientoStockMapper) {
        this.movimientoStockRepository = movimientoStockRepository;
        this.movimientoStockMapper = movimientoStockMapper;
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockDTO> listarMovimientos(int skip, int limit) {
        Pageable pageable = PageRequest.of(skip / Math.max(limit, 1), limit);
        return movimientoStockRepository.findAllByOrderByFechaDesc(pageable).stream()
                .map(movimientoStockMapper::toDTO)
                .toList();
    }
}
