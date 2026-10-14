package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.model.Propietario;
import com.tecsolutions.Veterinaria.repository.PropietarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PropietarioService {

    @Autowired
    private PropietarioRepository propietarioRepository;

    public List<Propietario> listarTodos() {
        return propietarioRepository.findAll();
    }

    public Optional<Propietario> buscarPorId(Long id) {
        return propietarioRepository.findById(id);
    }

    public Propietario guardar(Propietario propietario) {
        return propietarioRepository.save(propietario);
    }

    public Propietario actualizar(Long id, Propietario propietarioActualizado) {
        return propietarioRepository.findById(id).map(propietario -> {
            propietario.setNombre(propietarioActualizado.getNombre());
            propietario.setApellido(propietarioActualizado.getApellido());
            propietario.setTelefono(propietarioActualizado.getTelefono());
            propietario.setEmail(propietarioActualizado.getEmail());
            propietario.setDireccion(propietarioActualizado.getDireccion());
            return propietarioRepository.save(propietario);
        }).orElseThrow(() -> new RuntimeException("Propietario no encontrado con id: " + id));
    }

    public void eliminar(Long id) {
        propietarioRepository.deleteById(id);
    }
}
