package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.UsuarioCreateDTO;
import com.tecsolutions.Veterinaria.dto.UsuarioDTO;
import com.tecsolutions.Veterinaria.dto.UsuarioUpdateDTO;
import com.tecsolutions.Veterinaria.mapper.UsuarioMapper;
import com.tecsolutions.Veterinaria.model.RolEnum;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toDTO)
                .toList();
    }

    public UsuarioDTO crearUsuario(UsuarioCreateDTO dto) {
        RolEnum rol;
        try {
            rol = RolEnum.fromValue(dto.getRol());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido");
        }

        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de usuario ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setHashedPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(rol != null ? rol : RolEnum.RECEPCIONISTA);
        usuario.setIsActive(true);
        usuario.setEmail(dto.getEmail());

        Usuario guardado = usuarioRepository.save(usuario);
        return usuarioMapper.toDTO(guardado);
    }

    public UsuarioDTO actualizarUsuario(Long id, UsuarioUpdateDTO dto, Usuario currentUser) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (dto.getUsername() != null && !dto.getUsername().isBlank() && !dto.getUsername().equals(usuario.getUsername())) {
            if (usuarioRepository.existsByUsername(dto.getUsername())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de usuario ya existe");
            }
            usuario.setUsername(dto.getUsername());
        }

        if (dto.getRol() != null && !dto.getRol().isBlank()) {
            try {
                RolEnum rol = RolEnum.fromValue(dto.getRol());
                usuario.setRol(rol);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol inválido");
            }
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setHashedPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getIsActive() != null) {
            if (currentUser != null && id.equals(currentUser.getId()) && Boolean.FALSE.equals(dto.getIsActive())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes desactivar tu propia cuenta");
            }
            usuario.setIsActive(dto.getIsActive());
        }

        if (dto.getEmail() != null) {
            usuario.setEmail(dto.getEmail());
        }

        Usuario actualizado = usuarioRepository.save(usuario);
        return usuarioMapper.toDTO(actualizado);
    }

    public Map<String, String> eliminarUsuario(Long id, Usuario currentUser) {
        if (currentUser != null && id.equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuarioRepository.delete(usuario);
        return Map.of("detail", "Usuario eliminado");
    }
}
