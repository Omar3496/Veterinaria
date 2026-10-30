package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.MascotaDTO;
import com.tecsolutions.Veterinaria.mapper.MascotaMapper;
import com.tecsolutions.Veterinaria.model.Cliente;
import com.tecsolutions.Veterinaria.model.Mascota;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.repository.ClienteRepository;
import com.tecsolutions.Veterinaria.repository.MascotaRepository;
import com.tecsolutions.Veterinaria.repository.UsuarioRepository;
import static com.tecsolutions.Veterinaria.security.SecurityUtils.isClienteRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MascotaMapper mascotaMapper;

    public MascotaService(
            MascotaRepository mascotaRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            MascotaMapper mascotaMapper) {
        this.mascotaRepository = mascotaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.mascotaMapper = mascotaMapper;
    }

    public MascotaDTO crearMascotaParaCliente(Long clienteId, MascotaDTO dto) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado"
                ));

        Mascota mascota = mascotaMapper.toEntity(dto);
        mascota.setPropietario(cliente);
        if (mascota.getActivo() == null) {
            mascota.setActivo(true);
        }

        Mascota guardada = mascotaRepository.save(mascota);
        return mascotaMapper.toDTO(guardada);
    }

    @Transactional(readOnly = true)
    public List<MascotaDTO> listarMascotas(int skip, int limit, String currentUsername, String currentRole) {
        if (isClienteRole(currentRole)) {
            if (currentUsername == null) {
                return Collections.emptyList();
            }
            Usuario usuario = usuarioRepository.findByUsername(currentUsername).orElse(null);
            if (usuario == null) {
                return Collections.emptyList();
            }
            Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
            if (cliente == null) {
                return Collections.emptyList();
            }
            return mascotaRepository.findByPropietarioId(cliente.getId()).stream()
                    .skip(Math.max(0, skip))
                    .limit(limit > 0 ? limit : 100)
                    .map(mascotaMapper::toDTO)
                    .toList();
        }

        return mascotaRepository.findAll().stream()
                .skip(Math.max(0, skip))
                .limit(limit > 0 ? limit : 100)
                .map(mascotaMapper::toDTO)
                .toList();
    }

    public MascotaDTO actualizarFotoMascota(Long mascotaId, String fotoBase64, String currentUsername, String currentRole) {
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Mascota no encontrada"
                ));

        if (isClienteRole(currentRole)) {
            Usuario usuario = currentUsername != null ? usuarioRepository.findByUsername(currentUsername).orElse(null) : null;
            Cliente cliente = usuario != null ? clienteRepository.findByUsuarioId(usuario.getId()).orElse(null) : null;
            if (cliente == null || mascota.getPropietario() == null || !cliente.getId().equals(mascota.getPropietario().getId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No puedes modificar una mascota que no te pertenece"
                );
            }
        }

        mascota.setFotoUrl(fotoBase64);
        Mascota guardada = mascotaRepository.save(mascota);
        return mascotaMapper.toDTO(guardada);
    }
}
