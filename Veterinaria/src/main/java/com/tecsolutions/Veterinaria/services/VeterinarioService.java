package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.VeterinarioDTO;
import com.tecsolutions.Veterinaria.model.Veterinario;
import com.tecsolutions.Veterinaria.repository.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VeterinarioService {

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    public List<VeterinarioDTO> listarTodos() {
        return veterinarioRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public Optional<VeterinarioDTO> buscarPorId(Long id) {
        return veterinarioRepository.findById(id)
                .map(this::convertirADTO);
    }

    public VeterinarioDTO guardar(VeterinarioDTO veterinarioDTO) {
        Veterinario veterinario = convertirAEntidad(veterinarioDTO);
        Veterinario guardado = veterinarioRepository.save(veterinario);
        return convertirADTO(guardado);
    }

    public VeterinarioDTO actualizar(Long id, VeterinarioDTO veterinarioActualizado) {
        return veterinarioRepository.findById(id).map(veterinario -> {
            veterinario.setNombre(veterinarioActualizado.getNombre());
            veterinario.setApellido(veterinarioActualizado.getApellido());
            veterinario.setEspecialidad(veterinarioActualizado.getEspecialidad());
            veterinario.setTelefono(veterinarioActualizado.getTelefono());
            veterinario.setEmail(veterinarioActualizado.getEmail());
            veterinario.setNumeroColegiatura(veterinarioActualizado.getNumeroColegiatura());
            return convertirADTO(veterinarioRepository.save(veterinario));
        }).orElseThrow(() -> new RuntimeException("Veterinario no encontrado con id: " + id));
    }

    public void eliminar(Long id) {
        veterinarioRepository.deleteById(id);
    }

    private VeterinarioDTO convertirADTO(Veterinario veterinario) {
        return new VeterinarioDTO(
                veterinario.getId(),
                veterinario.getNombre(),
                veterinario.getApellido(),
                veterinario.getEspecialidad(),
                veterinario.getTelefono(),
                veterinario.getEmail(),
                veterinario.getNumeroColegiatura()
        );
    }

    private Veterinario convertirAEntidad(VeterinarioDTO dto) {
        Veterinario veterinario = new Veterinario();
        veterinario.setNombre(dto.getNombre());
        veterinario.setApellido(dto.getApellido());
        veterinario.setEspecialidad(dto.getEspecialidad());
        veterinario.setTelefono(dto.getTelefono());
        veterinario.setEmail(dto.getEmail());
        veterinario.setNumeroColegiatura(dto.getNumeroColegiatura());
        return veterinario;
    }
}
