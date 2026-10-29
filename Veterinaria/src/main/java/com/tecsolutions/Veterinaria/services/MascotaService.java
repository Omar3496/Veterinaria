package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.MascotaDTO;
import com.tecsolutions.Veterinaria.model.Mascota;
import com.tecsolutions.Veterinaria.model.Propietario;
import com.tecsolutions.Veterinaria.repository.MascotaRepository;
import com.tecsolutions.Veterinaria.repository.PropietarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MascotaService {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    public List<MascotaDTO> listarTodas() {
        return mascotaRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public Optional<MascotaDTO> buscarPorId(Long id) {
        return mascotaRepository.findById(id)
                .map(this::convertirADTO);
    }

    public List<MascotaDTO> buscarPorPropietario(Long propietarioId) {
        return mascotaRepository.findByPropietarioId(propietarioId)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<MascotaDTO> buscarPorEspecie(String especie) {
        return mascotaRepository.findByEspecie(especie)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public MascotaDTO guardar(MascotaDTO mascotaDTO) {
        Mascota mascota = convertirAEntidad(mascotaDTO);
        Mascota guardada = mascotaRepository.save(mascota);
        return convertirADTO(guardada);
    }

    public MascotaDTO actualizar(Long id, MascotaDTO mascotaDTO) {
        return mascotaRepository.findById(id).map(mascota -> {
            mascota.setNombre(mascotaDTO.getNombre());
            mascota.setEspecie(mascotaDTO.getEspecie());
            mascota.setRaza(mascotaDTO.getRaza());
            mascota.setEdad(mascotaDTO.getEdad());

            if (mascotaDTO.getPropietarioId() != null) {
                Propietario propietario = propietarioRepository.findById(mascotaDTO.getPropietarioId())
                        .orElseThrow(() -> new RuntimeException("Propietario no encontrado con id: " + mascotaDTO.getPropietarioId()));
                mascota.setPropietario(propietario);
            } else {
                mascota.setPropietario(null);
            }

            return convertirADTO(mascotaRepository.save(mascota));
        }).orElseThrow(() -> new RuntimeException("Mascota no encontrada con id: " + id));
    }

    public void eliminar(Long id) {
        mascotaRepository.deleteById(id);
    }

    private MascotaDTO convertirADTO(Mascota mascota) {
        Long propietarioId = null;
        String propietarioNombre = null;

        if (mascota.getPropietario() != null) {
            propietarioId = mascota.getPropietario().getId();
            propietarioNombre = mascota.getPropietario().getNombre() + " " + mascota.getPropietario().getApellido();
        }

        return new MascotaDTO(
                mascota.getId(),
                mascota.getNombre(),
                mascota.getEspecie(),
                mascota.getRaza(),
                mascota.getEdad(),
                propietarioId,
                propietarioNombre
        );
    }

    private Mascota convertirAEntidad(MascotaDTO dto) {
        Mascota mascota = new Mascota();
        mascota.setNombre(dto.getNombre());
        mascota.setEspecie(dto.getEspecie());
        mascota.setRaza(dto.getRaza());
        mascota.setEdad(dto.getEdad());

        if (dto.getPropietarioId() != null) {
            Propietario propietario = propietarioRepository.findById(dto.getPropietarioId())
                    .orElseThrow(() -> new RuntimeException("Propietario no encontrado con id: " + dto.getPropietarioId()));
            mascota.setPropietario(propietario);
        }

        return mascota;
    }
}
