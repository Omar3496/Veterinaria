package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.PropietarioDTO;
import com.tecsolutions.Veterinaria.model.Propietario;
import com.tecsolutions.Veterinaria.repository.PropietarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PropietarioService {

    @Autowired
    private PropietarioRepository propietarioRepository;

    public List<PropietarioDTO> listarTodos() {
        return propietarioRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public Optional<PropietarioDTO> buscarPorId(Long id) {
        return propietarioRepository.findById(id)
                .map(this::convertirADTO);
    }

    public PropietarioDTO guardar(PropietarioDTO propietarioDTO) {
        Propietario propietario = convertirAEntidad(propietarioDTO);
        Propietario guardado = propietarioRepository.save(propietario);
        return convertirADTO(guardado);
    }

    public PropietarioDTO actualizar(Long id, PropietarioDTO propietarioActualizado) {
        return propietarioRepository.findById(id).map(propietario -> {
            propietario.setNombre(propietarioActualizado.getNombre());
            propietario.setApellido(propietarioActualizado.getApellido());
            propietario.setTelefono(propietarioActualizado.getTelefono());
            propietario.setEmail(propietarioActualizado.getEmail());
            propietario.setDireccion(propietarioActualizado.getDireccion());
            return convertirADTO(propietarioRepository.save(propietario));
        }).orElseThrow(() -> new RuntimeException("Propietario no encontrado con id: " + id));
    }

    public void eliminar(Long id) {
        propietarioRepository.deleteById(id);
    }

    private PropietarioDTO convertirADTO(Propietario propietario) {
        return new PropietarioDTO(
                propietario.getId(),
                propietario.getNombre(),
                propietario.getApellido(),
                propietario.getTelefono(),
                propietario.getEmail(),
                propietario.getDireccion()
        );
    }

    private Propietario convertirAEntidad(PropietarioDTO dto) {
        Propietario propietario = new Propietario();
        propietario.setNombre(dto.getNombre());
        propietario.setApellido(dto.getApellido());
        propietario.setTelefono(dto.getTelefono());
        propietario.setEmail(dto.getEmail());
        propietario.setDireccion(dto.getDireccion());
        return propietario;
    }
}
