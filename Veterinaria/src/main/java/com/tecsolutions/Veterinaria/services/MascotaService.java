package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.model.Mascota;
import com.tecsolutions.Veterinaria.repository.MascotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MascotaService {

    @Autowired
    private MascotaRepository mascotaRepository;

    public List<Mascota> listarTodas() {
        return mascotaRepository.findAll();
    }

    public Optional<Mascota> buscarPorId(Long id) {
        return mascotaRepository.findById(id);
    }

    public List<Mascota> buscarPorPropietario(Long propietarioId) {
        return mascotaRepository.findByPropietarioId(propietarioId);
    }

    public List<Mascota> buscarPorEspecie(String especie) {
        return mascotaRepository.findByEspecie(especie);
    }

    public Mascota guardar(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }

    public Mascota actualizar(Long id, Mascota mascotaActualizada) {
        return mascotaRepository.findById(id).map(mascota -> {
            mascota.setNombre(mascotaActualizada.getNombre());
            mascota.setEspecie(mascotaActualizada.getEspecie());
            mascota.setRaza(mascotaActualizada.getRaza());
            mascota.setEdad(mascotaActualizada.getEdad());
            mascota.setPropietario(mascotaActualizada.getPropietario());
            return mascotaRepository.save(mascota);
        }).orElseThrow(() -> new RuntimeException("Mascota no encontrada con id: " + id));
    }

    public void eliminar(Long id) {
        mascotaRepository.deleteById(id);
    }
}
