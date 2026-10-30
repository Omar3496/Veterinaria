package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.*;
import com.tecsolutions.Veterinaria.mapper.CitaMapper;
import com.tecsolutions.Veterinaria.model.*;
import com.tecsolutions.Veterinaria.repository.*;
import static com.tecsolutions.Veterinaria.security.SecurityUtils.isClienteRole;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CitaService {

    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ExamenAuxiliarRepository examenAuxiliarRepository;
    private final OrdenExamenRepository ordenExamenRepository;
    private final ConfiguracionService configuracionService;
    private final CitaMapper citaMapper;

    public CitaService(
            CitaRepository citaRepository,
            MascotaRepository mascotaRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            ExamenAuxiliarRepository examenAuxiliarRepository,
            OrdenExamenRepository ordenExamenRepository,
            ConfiguracionService configuracionService,
            CitaMapper citaMapper) {
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.examenAuxiliarRepository = examenAuxiliarRepository;
        this.ordenExamenRepository = ordenExamenRepository;
        this.configuracionService = configuracionService;
        this.citaMapper = citaMapper;
    }

    @Transactional(readOnly = true)
    public DisponibilidadDiaDTO obtenerDisponibilidad(LocalDate fecha, Long veterinarioId) {
        HorarioConfigDTO cfg = configuracionService.obtenerHorarios();
        int isoDia = fecha.getDayOfWeek().getValue();

        if (cfg.getDias() == null || !cfg.getDias().contains(isoDia)) {
            return DisponibilidadDiaDTO.builder()
                    .fecha(fecha)
                    .dia(isoDia)
                    .disponible(false)
                    .horarios(new ArrayList<>())
                    .build();
        }

        LocalDateTime startOfDay = fecha.atStartOfDay();
        LocalDateTime endOfDay = fecha.atTime(LocalTime.MAX);
        List<Cita> citasDia;
        if (veterinarioId != null) {
            citasDia = citaRepository.findByVeterinarioIdAndFechaHoraBetweenAndEstadoNot(
                    veterinarioId, startOfDay, endOfDay, EstadoCita.CANCELADA);
        } else {
            citasDia = citaRepository.findByFechaHoraBetweenAndEstadoNot(
                    startOfDay, endOfDay, EstadoCita.CANCELADA);
        }

        LocalTime inicio = LocalTime.parse(cfg.getHoraInicio());
        LocalTime fin = LocalTime.parse(cfg.getHoraFin());
        int duracion = cfg.getDuracionMin();
        LocalDateTime ahora = LocalDateTime.now();

        List<HorarioSlotDTO> horarios = new ArrayList<>();
        LocalDateTime current = LocalDateTime.of(fecha, inicio);
        LocalDateTime endDateTime = LocalDateTime.of(fecha, fin);

        while (current.isBefore(endDateTime)) {
            LocalDateTime slotInicio = current;
            LocalDateTime slotFin = current.plusMinutes(duracion);

            boolean ocupado = slotInicio.isBefore(ahora) || citasDia.stream().anyMatch(c ->
                    !c.getFechaHora().isBefore(slotInicio) && c.getFechaHora().isBefore(slotFin));

            horarios.add(HorarioSlotDTO.builder()
                    .hora(slotInicio.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .disponible(!ocupado)
                    .build());

            current = current.plusMinutes(duracion);
        }

        return DisponibilidadDiaDTO.builder()
                .fecha(fecha)
                .dia(isoDia)
                .disponible(true)
                .horarios(horarios)
                .build();
    }

    public CitaDTO agendarCita(CitaDTO dto, String currentUsername, String currentRole) {
        if (dto.getFechaHora() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha y hora de la cita es obligatoria");
        }

        Mascota mascota;
        if (isClienteRole(currentRole)) {
            Usuario usuario = usuarioRepository.findByUsername(currentUsername).orElse(null);
            Cliente cliente = (usuario != null) ? clienteRepository.findByUsuarioId(usuario.getId()).orElse(null) : null;
            if (cliente == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No existe un perfil de cliente asociado a este usuario");
            }
            if (dto.getMascotaId() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para agendar citas para mascotas que no te pertenecen");
            }
            mascota = mascotaRepository.findById(dto.getMascotaId()).orElse(null);
            if (mascota == null || mascota.getPropietario() == null || !mascota.getPropietario().getId().equals(cliente.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para agendar citas para mascotas que no te pertenecen");
            }
        } else {
            if (dto.getMascotaId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe especificar una mascota");
            }
            mascota = mascotaRepository.findById(dto.getMascotaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));
        }

        ExamenAuxiliar examen = null;
        if ("Examen auxiliar".equalsIgnoreCase(dto.getTipo())) {
            if (dto.getExamenAuxiliarId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar un examen auxiliar válido para este tipo de cita");
            }
            examen = examenAuxiliarRepository.findById(dto.getExamenAuxiliarId()).orElse(null);
            if (examen == null || Boolean.FALSE.equals(examen.getActivo())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar un examen auxiliar válido para este tipo de cita");
            }
        }

        HorarioConfigDTO cfg = configuracionService.obtenerHorarios();
        int duracion = cfg.getDuracionMin();
        LocalDateTime minTime = dto.getFechaHora().minusMinutes(duracion - 1);
        LocalDateTime maxTime = dto.getFechaHora().plusMinutes(duracion);

        List<Cita> conflictCandidates = citaRepository.findByFechaHoraBetweenAndEstadoNot(minTime, maxTime, EstadoCita.CANCELADA);
        boolean conflict = conflictCandidates.stream().anyMatch(c -> {
            if (c.getFechaHora().isEqual(maxTime)) {
                return false;
            }
            if (dto.getVeterinarioId() != null) {
                return c.getVeterinario() != null && c.getVeterinario().getId().equals(dto.getVeterinarioId());
            }
            return true;
        });

        if (conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese horario ya está ocupado para el veterinario seleccionado. Elige otra hora.");
        }

        EstadoCita estadoInicial = isClienteRole(currentRole) ? EstadoCita.PENDIENTE : EstadoCita.PROGRAMADA;

        Cita cita = new Cita();
        cita.setFechaHora(dto.getFechaHora());
        cita.setMotivo(dto.getMotivo());
        cita.setTipo(dto.getTipo() != null ? dto.getTipo() : "Consulta médica");
        cita.setEstado(estadoInicial);
        cita.setMascota(mascota);

        if (dto.getVeterinarioId() != null) {
            Usuario vet = usuarioRepository.findById(dto.getVeterinarioId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinario no encontrado"));
            cita.setVeterinario(vet);
        }

        if (examen != null) {
            cita.setExamenAuxiliar(examen);
        }

        Cita citaGuardada = citaRepository.save(cita);

        if ("Examen auxiliar".equalsIgnoreCase(dto.getTipo()) && examen != null) {
            OrdenExamen orden = new OrdenExamen();
            orden.setCita(citaGuardada);
            orden.setExamenAuxiliar(examen);
            orden.setEstado("PENDIENTE");
            orden.setObservaciones(dto.getMotivo());
            orden.setFecha(LocalDateTime.now());
            citaGuardada.setOrdenExamen(orden);
            ordenExamenRepository.save(orden);
        }

        return citaMapper.toDTO(citaGuardada);
    }

    @Transactional(readOnly = true)
    public List<CitaDTO> listarCitas(int skip, int limit) {
        return citaRepository.findAll().stream()
                .skip(Math.max(0, skip))
                .limit(limit > 0 ? limit : 100)
                .map(citaMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CitaConMascotaDTO> listarCitasMias(int skip, int limit, String currentUsername) {
        Usuario usuario = usuarioRepository.findByUsername(currentUsername).orElse(null);
        Cliente cliente = (usuario != null) ? clienteRepository.findByUsuarioId(usuario.getId()).orElse(null) : null;
        if (cliente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un perfil de cliente asociado a este usuario");
        }

        return citaRepository.findByMascotaPropietarioIdOrderByFechaHoraDesc(cliente.getId(), Pageable.unpaged()).stream()
                .skip(Math.max(0, skip))
                .limit(limit > 0 ? limit : 100)
                .map(citaMapper::toConMascotaDTO)
                .toList();
    }

    public CitaDTO reprogramarCita(Long id, ReprogramarCitaRequestDTO req, String currentUsername, String currentRole) {
        Cita dbCita = citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        if (isClienteRole(currentRole)) {
            Usuario usuario = usuarioRepository.findByUsername(currentUsername).orElse(null);
            Cliente cliente = (usuario != null) ? clienteRepository.findByUsuarioId(usuario.getId()).orElse(null) : null;
            if (cliente == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No existe un perfil de cliente asociado a este usuario");
            }
            if (dbCita.getMascota() == null || dbCita.getMascota().getPropietario() == null
                    || !dbCita.getMascota().getPropietario().getId().equals(cliente.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para reprogramar esta cita");
            }
        }

        if (dbCita.getEstado() == EstadoCita.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes reprogramar una cita cancelada");
        }

        if (req.getFechaHora() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha y hora de la cita es obligatoria");
        }

        HorarioConfigDTO cfg = configuracionService.obtenerHorarios();
        int duracion = cfg.getDuracionMin();
        LocalDateTime minTime = req.getFechaHora().minusMinutes(duracion - 1);
        LocalDateTime maxTime = req.getFechaHora().plusMinutes(duracion);

        List<Cita> conflictCandidates = citaRepository.findByFechaHoraBetweenAndEstadoNot(minTime, maxTime, EstadoCita.CANCELADA);
        boolean conflict = conflictCandidates.stream()
                .filter(c -> !c.getId().equals(id))
                .anyMatch(c -> {
                    if (c.getFechaHora().isEqual(maxTime)) {
                        return false;
                    }
                    if (dbCita.getVeterinario() != null) {
                        return c.getVeterinario() != null && c.getVeterinario().getId().equals(dbCita.getVeterinario().getId());
                    }
                    return true;
                });

        if (conflict) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese horario ya está ocupado para el veterinario seleccionado. Elige otra hora.");
        }

        dbCita.setFechaHora(req.getFechaHora());
        if (req.getMotivo() != null && !req.getMotivo().isBlank()) {
            dbCita.setMotivo(req.getMotivo());
        }

        Cita guardada = citaRepository.save(dbCita);
        return citaMapper.toDTO(guardada);
    }

    public CitaDTO cancelarCitaCliente(Long id, CancelarCitaRequestDTO req, String currentUsername) {
        Usuario usuario = usuarioRepository.findByUsername(currentUsername).orElse(null);
        Cliente cliente = (usuario != null) ? clienteRepository.findByUsuarioId(usuario.getId()).orElse(null) : null;
        if (cliente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un perfil de cliente asociado a este usuario");
        }

        Cita dbCita = citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        if (dbCita.getMascota() == null || dbCita.getMascota().getPropietario() == null
                || !dbCita.getMascota().getPropietario().getId().equals(cliente.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para cancelar esta cita");
        }

        if (dbCita.getEstado() == EstadoCita.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cita ya se encuentra cancelada");
        }

        dbCita.setEstado(EstadoCita.CANCELADA);
        dbCita.setMotivoCancelacion(req != null ? req.getMotivoCancelacion() : null);

        Cita guardada = citaRepository.save(dbCita);
        return citaMapper.toDTO(guardada);
    }

    public CitaDTO actualizarEstadoCita(Long id, String estadoStr, String motivoCancelacion) {
        Cita dbCita = citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        EstadoCita nuevoEstado;
        try {
            nuevoEstado = EstadoCita.fromValue(estadoStr);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de cita inválido: " + estadoStr);
        }

        if (dbCita.getEstado() == EstadoCita.CANCELADA && nuevoEstado == EstadoCita.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cita ya se encuentra cancelada");
        }

        if (nuevoEstado == EstadoCita.CANCELADA && (motivoCancelacion == null || motivoCancelacion.trim().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe indicar el motivo de cancelación");
        }

        dbCita.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoCita.CANCELADA) {
            dbCita.setMotivoCancelacion(motivoCancelacion.trim());
        }

        Cita guardada = citaRepository.save(dbCita);
        return citaMapper.toDTO(guardada);
    }

    public Map<String, Object> eliminarCita(Long id) {
        Cita dbCita = citaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        citaRepository.delete(dbCita);
        return Map.of("detail", "Cita eliminada");
    }
}
