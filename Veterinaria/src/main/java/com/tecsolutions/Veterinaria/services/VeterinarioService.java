package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.model.Veterinario;
import com.tecsolutions.Veterinaria.repository.VeterinarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VeterinarioService {

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    public List<Veterinario> listarTodos() {
        return veterinarioRepository.findAll();
    }

    public Optional<Veterinario> buscarPorId(Long id) {
        return veterinarioRepository.findById(id);
    }

    public Veterinario guardar(Veterinario veterinario) {
        return veterinarioRepository.save(veterinario);
    }

    public Veterinario actualizar(Long id, Veterinario veterinarioActualizado) {
        return veterinarioRepository.findById(id).map(veterinario -> {
            veterinario.setNombre(veterinarioActualizado.getNombre());
            veterinario.setApellido(veterinarioActualizado.getApellido());
            veterinario.setEspecialidad(veterinarioActualizado.getEspecialidad());
            veterinario.setTelefono(veterinarioActualizado.getTelefono());
            veterinario.setEmail(veterinarioActualizado.getEmail());
            veterinario.setNumeroColegiatura(veterinarioActualizado.getNumeroColegiatura());
            return veterinarioRepository.save(veterinario);
        }).orElseThrow(() -> new RuntimeException("Veterinario no encontrado con id: " + id));
    }

    public void eliminar(Long id) {
        veterinarioRepository.deleteById(id);
    }
}
