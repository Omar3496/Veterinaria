package com.tecsolutions.Veterinaria.mapper;

import com.tecsolutions.Veterinaria.dto.UsuarioDTO;
import com.tecsolutions.Veterinaria.model.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .rol(usuario.getRol())
                .isActive(usuario.getIsActive())
                .email(usuario.getEmail())
                .build();
    }
}
