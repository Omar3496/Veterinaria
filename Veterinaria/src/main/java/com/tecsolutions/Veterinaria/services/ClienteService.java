package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.ClienteDTO;
import com.tecsolutions.Veterinaria.mapper.ClienteMapper;
import com.tecsolutions.Veterinaria.model.Cliente;
import com.tecsolutions.Veterinaria.model.RolEnum;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.repository.ClienteRepository;
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
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteMapper clienteMapper;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            ClienteMapper clienteMapper,
            PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteMapper = clienteMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> listarClientes(int skip, int limit) {
        return clienteRepository.findAll().stream()
                .skip(Math.max(0, skip))
                .limit(limit > 0 ? limit : 100)
                .map(clienteMapper::toDTO)
                .toList();
    }

    public ClienteDTO crearCliente(ClienteDTO dto) {
        boolean hasUsername = dto.getUsername() != null && !dto.getUsername().isBlank();
        boolean hasPassword = dto.getPassword() != null && !dto.getPassword().isBlank();

        if (hasUsername != hasPassword) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar usuario y contraseña para crear la cuenta de acceso"
            );
        }

        if (hasUsername) {
            if (usuarioRepository.existsByUsername(dto.getUsername())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El usuario ya existe"
                );
            }
        }

        boolean dniExists = dto.getDni() != null && !dto.getDni().isBlank()
                && clienteRepository.findByDni(dto.getDni()).isPresent();
        boolean telExists = dto.getTelefono() != null && !dto.getTelefono().isBlank()
                && clienteRepository.findByTelefono(dto.getTelefono()).isPresent();

        if (dniExists || telExists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El DNI o Teléfono ya está registrado"
            );
        }

        Usuario nuevoUsuario = null;
        if (hasUsername && hasPassword) {
            nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(dto.getUsername());
            nuevoUsuario.setHashedPassword(passwordEncoder.encode(dto.getPassword()));
            nuevoUsuario.setRol(RolEnum.CLIENTE);
            nuevoUsuario.setIsActive(true);
            nuevoUsuario.setEmail(dto.getEmail());
            nuevoUsuario = usuarioRepository.save(nuevoUsuario);
        }

        Cliente cliente = clienteMapper.toEntity(dto);
        if (nuevoUsuario != null) {
            cliente.setUsuario(nuevoUsuario);
        }

        Cliente guardado = clienteRepository.save(cliente);
        return clienteMapper.toDTO(guardado);
    }

    public Map<String, String> eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado"
                ));

        clienteRepository.delete(cliente);
        return Map.of("detail", "Cliente eliminado");
    }

    @Transactional(readOnly = true)
    public ClienteDTO obtenerClienteActual(String currentUsername) {
        Usuario usuario = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un perfil de cliente asociado a este usuario"
                ));

        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un perfil de cliente asociado a este usuario"
                ));

        return clienteMapper.toDTO(cliente);
    }

    public ClienteDTO actualizarFotoClienteActual(String currentUsername, String fotoBase64) {
        Usuario usuario = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un perfil de cliente asociado a este usuario"
                ));

        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe un perfil de cliente asociado a este usuario"
                ));

        cliente.setFotoUrl(fotoBase64);
        Cliente guardado = clienteRepository.save(cliente);
        return clienteMapper.toDTO(guardado);
    }
}
