package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.ClienteDTO;
import com.tecsolutions.Veterinaria.dto.MascotaDTO;
import com.tecsolutions.Veterinaria.model.Cliente;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClienteMapper {

    private final MascotaMapper mascotaMapper;

    public ClienteMapper(MascotaMapper mascotaMapper) {
        this.mascotaMapper = mascotaMapper;
    }

    public ClienteDTO toDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }
        List<MascotaDTO> mascotasDTO = new ArrayList<>();
        if (cliente.getMascotas() != null) {
            for (var m : cliente.getMascotas()) {
                mascotasDTO.add(mascotaMapper.toDTO(m));
            }
        }
        return ClienteDTO.builder()
                .id(cliente.getId())
                .dni(cliente.getDni())
                .nombre(cliente.getNombre())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .fotoUrl(cliente.getFotoUrl())
                .usuarioId(cliente.getUsuario() != null ? cliente.getUsuario().getId() : null)
                .mascotas(mascotasDTO)
                .build();
    }

    public Cliente toEntity(ClienteDTO dto) {
        if (dto == null) {
            return null;
        }
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setDni(dto.getDni());
        cliente.setNombre(dto.getNombre());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setFotoUrl(dto.getFotoUrl());
        return cliente;
    }
}
