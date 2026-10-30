package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.ExamenAuxiliarDTO;
import com.tecsolutions.Veterinaria.mapper.ExamenAuxiliarMapper;
import com.tecsolutions.Veterinaria.model.ExamenAuxiliar;
import com.tecsolutions.Veterinaria.repository.ExamenAuxiliarRepository;
import com.tecsolutions.Veterinaria.repository.OrdenExamenRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ExamenAuxiliarService {

    private final ExamenAuxiliarRepository examenAuxiliarRepository;
    private final OrdenExamenRepository ordenExamenRepository;
    private final ExamenAuxiliarMapper examenAuxiliarMapper;

    public ExamenAuxiliarService(
            ExamenAuxiliarRepository examenAuxiliarRepository,
            OrdenExamenRepository ordenExamenRepository,
            ExamenAuxiliarMapper examenAuxiliarMapper) {
        this.examenAuxiliarRepository = examenAuxiliarRepository;
        this.ordenExamenRepository = ordenExamenRepository;
        this.examenAuxiliarMapper = examenAuxiliarMapper;
    }

    @Transactional(readOnly = true)
    public List<ExamenAuxiliarDTO> listarExamenes() {
        return examenAuxiliarRepository.findAllByOrderByNombreAsc().stream()
                .map(examenAuxiliarMapper::toDTO)
                .toList();
    }

    public ExamenAuxiliarDTO crearExamen(ExamenAuxiliarDTO dto) {
        String nombre = dto.getNombre() != null ? dto.getNombre().trim() : "";
        if (nombre.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del examen es obligatorio");
        }
        if (examenAuxiliarRepository.findByNombre(nombre).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un examen con ese nombre");
        }

        ExamenAuxiliar entity = examenAuxiliarMapper.toEntity(dto);
        entity.setNombre(nombre);
        if (entity.getActivo() == null) {
            entity.setActivo(true);
        }
        if (entity.getPrecio() == null) {
            entity.setPrecio(0.0);
        }

        ExamenAuxiliar guardado = examenAuxiliarRepository.save(entity);
        return examenAuxiliarMapper.toDTO(guardado);
    }

    public ExamenAuxiliarDTO actualizarExamen(Long id, ExamenAuxiliarDTO dto) {
        ExamenAuxiliar entity = examenAuxiliarRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Examen no encontrado"));

        String nombre = dto.getNombre() != null ? dto.getNombre().trim() : "";
        if (nombre.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del examen es obligatorio");
        }
        if (examenAuxiliarRepository.findByNombreAndIdNot(nombre, id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un examen con ese nombre");
        }

        entity.setNombre(nombre);
        entity.setDescripcion(dto.getDescripcion());
        entity.setPrecio(dto.getPrecio() != null ? dto.getPrecio() : 0.0);
        entity.setTiempoEstimado(dto.getTiempoEstimado());
        if (dto.getActivo() != null) {
            entity.setActivo(dto.getActivo());
        }

        ExamenAuxiliar guardado = examenAuxiliarRepository.save(entity);
        return examenAuxiliarMapper.toDTO(guardado);
    }

    public Map<String, Object> eliminarExamen(Long id) {
        ExamenAuxiliar entity = examenAuxiliarRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Examen no encontrado"));

        if (ordenExamenRepository.existsByExamenAuxiliarId(id)) {
            entity.setActivo(false);
            examenAuxiliarRepository.save(entity);
            return Map.of("detail", "Examen desactivado (ya tiene órdenes registradas)");
        }

        examenAuxiliarRepository.delete(entity);
        return Map.of("detail", "Examen eliminado");
    }
}
