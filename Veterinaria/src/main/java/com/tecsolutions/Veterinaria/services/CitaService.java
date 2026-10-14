package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.model.Cita;
import com.tecsolutions.Veterinaria.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    public Optional<Cita> buscarPorId(Long id) {
        return citaRepository.findById(id);
    }

    public List<Cita> buscarPorMascota(Long mascotaId) {
        return citaRepository.findByMascotaId(mascotaId);
    }

    public List<Cita> buscarPorVeterinario(Long veterinarioId) {
        return citaRepository.findByVeterinarioId(veterinarioId);
    }

    public List<Cita> buscarPorEstado(String estado) {
        return citaRepository.findByEstado(estado);
    }

    public Cita guardar(Cita cita) {
        return citaRepository.save(cita);
    }

    public Cita actualizar(Long id, Cita citaActualizada) {
        return citaRepository.findById(id).map(cita -> {
            cita.setFechaHora(citaActualizada.getFechaHora());
            cita.setMotivo(citaActualizada.getMotivo());
            cita.setEstado(citaActualizada.getEstado());
            cita.setObservaciones(citaActualizada.getObservaciones());
            cita.setMascota(citaActualizada.getMascota());
            cita.setVeterinario(citaActualizada.getVeterinario());
            return citaRepository.save(cita);
        }).orElseThrow(() -> new RuntimeException("Cita no encontrada con id: " + id));
    }

    public void eliminar(Long id) {
        citaRepository.deleteById(id);
    }
}
