package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.CitaDTO;
import com.tecsolutions.Veterinaria.model.Cita;
import com.tecsolutions.Veterinaria.model.Mascota;
import com.tecsolutions.Veterinaria.model.Veterinario;
import com.tecsolutions.Veterinaria.repository.CitaRepository;
import com.tecsolutions.Veterinaria.repository.MascotaRepository;
import com.tecsolutions.Veterinaria.repository.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    public List<CitaDTO> listarTodas() {
        return citaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public Optional<CitaDTO> buscarPorId(Long id) {
        return citaRepository.findById(id)
                .map(this::convertirADTO);
    }

    public List<CitaDTO> buscarPorMascota(Long mascotaId) {
        return citaRepository.findByMascotaId(mascotaId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<CitaDTO> buscarPorVeterinario(Long veterinarioId) {
        return citaRepository.findByVeterinarioId(veterinarioId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<CitaDTO> buscarPorEstado(String estado) {
        return citaRepository.findByEstado(estado)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public CitaDTO guardar(CitaDTO citaDTO) {
        Cita cita = convertirAEntidad(citaDTO);
        Cita guardada = citaRepository.save(cita);
        return convertirADTO(guardada);
    }

    public CitaDTO actualizar(Long id, CitaDTO citaActualizada) {
        return citaRepository.findById(id).map(cita -> {
            cita.setFechaHora(citaActualizada.getFechaHora());
            cita.setMotivo(citaActualizada.getMotivo());
            cita.setEstado(citaActualizada.getEstado());
            cita.setObservaciones(citaActualizada.getObservaciones());

            if (citaActualizada.getMascotaId() != null) {
                Mascota mascota = mascotaRepository.findById(citaActualizada.getMascotaId())
                        .orElseThrow(() -> new RuntimeException("Mascota no encontrada con id: " + citaActualizada.getMascotaId()));
                cita.setMascota(mascota);
            } else {
                cita.setMascota(null);
            }

            if (citaActualizada.getVeterinarioId() != null) {
                Veterinario veterinario = veterinarioRepository.findById(citaActualizada.getVeterinarioId())
                        .orElseThrow(() -> new RuntimeException("Veterinario no encontrado con id: " + citaActualizada.getVeterinarioId()));
                cita.setVeterinario(veterinario);
            } else {
                cita.setVeterinario(null);
            }

            return convertirADTO(citaRepository.save(cita));
        }).orElseThrow(() -> new RuntimeException("Cita no encontrada con id: " + id));
    }

    public void eliminar(Long id) {
        citaRepository.deleteById(id);
    }

    private CitaDTO convertirADTO(Cita cita) {
        Long mascotaId = null;
        String mascotaNombre = null;
        if (cita.getMascota() != null) {
            mascotaId = cita.getMascota().getId();
            mascotaNombre = cita.getMascota().getNombre();
        }

        Long veterinarioId = null;
        String veterinarioNombre = null;
        if (cita.getVeterinario() != null) {
            veterinarioId = cita.getVeterinario().getId();
            veterinarioNombre = cita.getVeterinario().getNombre() + " " + cita.getVeterinario().getApellido();
        }

        return new CitaDTO(
                cita.getId(),
                cita.getFechaHora(),
                cita.getMotivo(),
                cita.getEstado(),
                cita.getObservaciones(),
                mascotaId,
                mascotaNombre,
                veterinarioId,
                veterinarioNombre
        );
    }

    private Cita convertirAEntidad(CitaDTO dto) {
        Cita cita = new Cita();
        cita.setFechaHora(dto.getFechaHora());
        cita.setMotivo(dto.getMotivo());
        cita.setEstado(dto.getEstado());
        cita.setObservaciones(dto.getObservaciones());

        if (dto.getMascotaId() != null) {
            Mascota mascota = mascotaRepository.findById(dto.getMascotaId())
                    .orElseThrow(() -> new RuntimeException("Mascota no encontrada con id: " + dto.getMascotaId()));
            cita.setMascota(mascota);
        }

        if (dto.getVeterinarioId() != null) {
            Veterinario veterinario = veterinarioRepository.findById(dto.getVeterinarioId())
                    .orElseThrow(() -> new RuntimeException("Veterinario no encontrado con id: " + dto.getVeterinarioId()));
            cita.setVeterinario(veterinario);
        }

        return cita;
    }
}
