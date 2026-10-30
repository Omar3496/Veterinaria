package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.*;
import com.tecsolutions.Veterinaria.mapper.*;
import com.tecsolutions.Veterinaria.model.*;
import com.tecsolutions.Veterinaria.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final RecetaMedicaRepository recetaMedicaRepository;
    private final OrdenExamenRepository ordenExamenRepository;
    private final ReferenciaMedicaRepository referenciaMedicaRepository;
    private final VacunaRepository vacunaRepository;
    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final ExamenAuxiliarRepository examenAuxiliarRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    private final ConsultaMapper consultaMapper;
    private final RecetaMedicaMapper recetaMedicaMapper;
    private final OrdenExamenMapper ordenExamenMapper;
    private final ReferenciaMedicaMapper referenciaMedicaMapper;
    private final VacunaMapper vacunaMapper;

    public ConsultaService(
            ConsultaRepository consultaRepository,
            RecetaMedicaRepository recetaMedicaRepository,
            OrdenExamenRepository ordenExamenRepository,
            ReferenciaMedicaRepository referenciaMedicaRepository,
            VacunaRepository vacunaRepository,
            MascotaRepository mascotaRepository,
            CitaRepository citaRepository,
            ExamenAuxiliarRepository examenAuxiliarRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            ConsultaMapper consultaMapper,
            RecetaMedicaMapper recetaMedicaMapper,
            OrdenExamenMapper ordenExamenMapper,
            ReferenciaMedicaMapper referenciaMedicaMapper,
            VacunaMapper vacunaMapper) {
        this.consultaRepository = consultaRepository;
        this.recetaMedicaRepository = recetaMedicaRepository;
        this.ordenExamenRepository = ordenExamenRepository;
        this.referenciaMedicaRepository = referenciaMedicaRepository;
        this.vacunaRepository = vacunaRepository;
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
        this.examenAuxiliarRepository = examenAuxiliarRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.consultaMapper = consultaMapper;
        this.recetaMedicaMapper = recetaMedicaMapper;
        this.ordenExamenMapper = ordenExamenMapper;
        this.referenciaMedicaMapper = referenciaMedicaMapper;
        this.vacunaMapper = vacunaMapper;
    }

    public ConsultaDTO crearConsulta(Long mascotaId, ConsultaCreateDTO dto, String currentUsername) {
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));

        Usuario vet = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Consulta consulta = new Consulta();
        consulta.setMascota(mascota);
        consulta.setVeterinario(vet);
        consulta.setFecha(LocalDateTime.now());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        consulta.setPeso(dto.getPeso());
        if (dto.getCitaId() != null) {
            Cita cita = citaRepository.findById(dto.getCitaId()).orElse(null);
            consulta.setCita(cita);
        }
        consulta = consultaRepository.save(consulta);

        crearOrdenesConsulta(consulta, dto);
        return consultaMapper.toDTO(consulta);
    }

    public ConsultaDTO atenderCita(Long citaId, ConsultaCreateDTO dto, String currentUsername) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes atender una cita cancelada");
        }
        if (cita.getEstado() == EstadoCita.COMPLETADA) {
            Optional<Consulta> yaAtendida = consultaRepository.findFirstByCitaId(citaId);
            if (yaAtendida.isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Esta cita ya fue atendida");
            }
        }

        Usuario vet = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        cita.setEstado(EstadoCita.COMPLETADA);
        citaRepository.save(cita);

        Consulta consulta = new Consulta();
        consulta.setMascota(cita.getMascota());
        consulta.setVeterinario(vet);
        consulta.setCita(cita);
        consulta.setFecha(LocalDateTime.now());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        consulta.setPeso(dto.getPeso());
        consulta = consultaRepository.save(consulta);

        crearOrdenesConsulta(consulta, dto);

        OrdenExamen ordenCita = cita.getOrdenExamen();
        if (ordenCita == null) {
            ordenCita = ordenExamenRepository.findByCitaId(citaId).orElse(null);
        }
        if (ordenCita != null && "PENDIENTE".equalsIgnoreCase(ordenCita.getEstado())) {
            ordenCita.setEstado("REALIZADO");
            ordenExamenRepository.save(ordenCita);
        }

        return consultaMapper.toDTO(consulta);
    }

    private void crearOrdenesConsulta(Consulta consulta, ConsultaCreateDTO dto) {
        if (dto.getRecetas() != null) {
            for (RecetaMedicaCreateDTO rec : dto.getRecetas()) {
                if (rec.getNombreMedicamento() != null && !rec.getNombreMedicamento().trim().isEmpty()) {
                    RecetaMedica r = new RecetaMedica();
                    r.setConsulta(consulta);
                    r.setNombreMedicamento(rec.getNombreMedicamento().trim());
                    r.setDosis(rec.getDosis());
                    r.setCantidad(rec.getCantidad());
                    r.setIndicaciones(rec.getIndicaciones());
                    r.setFecha(LocalDateTime.now());
                    if (rec.getProductoId() != null) {
                        Producto prod = productoRepository.findById(rec.getProductoId()).orElse(null);
                        r.setProducto(prod);
                    }
                    r = recetaMedicaRepository.save(r);
                    consulta.getRecetas().add(r);
                }
            }
        }

        if (dto.getExamenes() != null) {
            for (OrdenExamenCreateDTO ex : dto.getExamenes()) {
                if (ex.getExamenAuxiliarId() == null) {
                    continue;
                }
                ExamenAuxiliar examen = examenAuxiliarRepository.findById(ex.getExamenAuxiliarId()).orElse(null);
                if (examen == null || Boolean.FALSE.equals(examen.getActivo())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Examen auxiliar inválido (id " + ex.getExamenAuxiliarId() + ")");
                }
                OrdenExamen orden = new OrdenExamen();
                orden.setConsulta(consulta);
                orden.setExamenAuxiliar(examen);
                orden.setResultado(ex.getResultado());
                orden.setObservaciones(ex.getObservaciones());
                orden.setFecha(LocalDateTime.now());
                boolean hasResultado = ex.getResultado() != null && !ex.getResultado().trim().isEmpty();
                orden.setEstado(hasResultado ? "REALIZADO" : "PENDIENTE");
                orden = ordenExamenRepository.save(orden);
                consulta.getExamenes().add(orden);
            }
        }

        if (dto.getReferencia() != null && dto.getReferencia().getCentroDestino() != null && !dto.getReferencia().getCentroDestino().trim().isEmpty()) {
            ReferenciaMedica ref = new ReferenciaMedica();
            ref.setConsulta(consulta);
            ref.setCentroDestino(dto.getReferencia().getCentroDestino().trim());
            ref.setEspecialidad(dto.getReferencia().getEspecialidad());
            ref.setMotivo(dto.getReferencia().getMotivo());
            ref.setEstado("ACTIVA");
            ref.setFecha(LocalDateTime.now());
            ref = referenciaMedicaRepository.save(ref);
            consulta.getReferencias().add(ref);
        }
    }

    @Transactional(readOnly = true)
    public HistorialMascotaDTO obtenerHistorialMascota(Long mascotaId, String currentUsername, String currentRole) {
        verificarMascotaCliente(mascotaId, currentUsername, currentRole);

        List<Vacuna> vacunas = vacunaRepository.findByMascotaIdOrderByFechaAplicacionDesc(mascotaId);
        List<Consulta> consultas = consultaRepository.findByMascotaIdOrderByFechaDesc(mascotaId);
        List<Long> consultaIds = consultas.stream().map(Consulta::getId).toList();

        List<RecetaMedica> recetas = consultaIds.isEmpty() ? List.of() : recetaMedicaRepository.findByConsultaIdInOrderByFechaDesc(consultaIds);
        List<OrdenExamen> examenes = obtenerExamenesDeMascota(mascotaId);
        List<ReferenciaMedica> referencias = consultaIds.isEmpty() ? List.of() : referenciaMedicaRepository.findByConsultaIdInOrderByFechaDesc(consultaIds);

        List<VacunaDTO> vacunasDTO = vacunas.stream().map(vacunaMapper::toDTO).toList();
        List<ConsultaDTO> consultasDTO = consultas.stream().map(consultaMapper::toDTO).toList();
        List<RecetaMedicaDTO> recetasDTO = recetas.stream().map(recetaMedicaMapper::toDTO).toList();
        List<OrdenExamenDTO> examenesDTO = examenes.stream().map(ordenExamenMapper::toDTO).toList();
        List<ReferenciaMedicaDTO> referenciasDTO = referencias.stream().map(referenciaMedicaMapper::toDTO).toList();

        return HistorialMascotaDTO.builder()
                .vacunas(vacunasDTO)
                .consultas(consultasDTO)
                .recetas(recetasDTO)
                .examenes(examenesDTO)
                .referencias(referenciasDTO)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RecetaMedicaDTO> listarRecetasMascota(Long mascotaId, String currentUsername, String currentRole) {
        verificarMascotaCliente(mascotaId, currentUsername, currentRole);
        List<Consulta> consultas = consultaRepository.findByMascotaIdOrderByFechaDesc(mascotaId);
        List<Long> consultaIds = consultas.stream().map(Consulta::getId).toList();
        if (consultaIds.isEmpty()) {
            return Collections.emptyList();
        }
        return recetaMedicaRepository.findByConsultaIdInOrderByFechaDesc(consultaIds).stream()
                .map(recetaMedicaMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenExamenDTO> listarExamenesMascota(Long mascotaId, String currentUsername, String currentRole) {
        verificarMascotaCliente(mascotaId, currentUsername, currentRole);
        return obtenerExamenesDeMascota(mascotaId).stream()
                .map(ordenExamenMapper::toDTO)
                .toList();
    }

    private List<OrdenExamen> obtenerExamenesDeMascota(Long mascotaId) {
        List<OrdenExamen> list1 = ordenExamenRepository.findByConsultaMascotaIdOrderByFechaDesc(mascotaId);
        List<OrdenExamen> list2 = ordenExamenRepository.findByCitaMascotaIdIn(List.of(mascotaId));
        if (list2.isEmpty()) {
            return list1;
        }
        if (list1.isEmpty()) {
            return list2;
        }
        Map<Long, OrdenExamen> unique = new LinkedHashMap<>();
        for (OrdenExamen o : list1) {
            unique.put(o.getId(), o);
        }
        for (OrdenExamen o : list2) {
            unique.putIfAbsent(o.getId(), o);
        }
        List<OrdenExamen> result = new ArrayList<>(unique.values());
        result.sort((a, b) -> {
            if (a.getFecha() == null && b.getFecha() == null) return 0;
            if (a.getFecha() == null) return 1;
            if (b.getFecha() == null) return -1;
            return b.getFecha().compareTo(a.getFecha());
        });
        return result;
    }

    @Transactional(readOnly = true)
    public List<ReferenciaMedicaDTO> listarReferenciasMascota(Long mascotaId, String currentUsername, String currentRole) {
        verificarMascotaCliente(mascotaId, currentUsername, currentRole);
        List<Consulta> consultas = consultaRepository.findByMascotaIdOrderByFechaDesc(mascotaId);
        List<Long> consultaIds = consultas.stream().map(Consulta::getId).toList();
        if (consultaIds.isEmpty()) {
            return Collections.emptyList();
        }
        return referenciaMedicaRepository.findByConsultaIdInOrderByFechaDesc(consultaIds).stream()
                .map(referenciaMedicaMapper::toDTO)
                .toList();
    }

    public OrdenExamenDTO actualizarResultadoExamen(Long ordenId, OrdenExamenUpdateDTO dto) {
        OrdenExamen orden = ordenExamenRepository.findById(ordenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden de examen no encontrada"));

        if (dto.getResultado() != null) {
            orden.setResultado(dto.getResultado());
        }
        if (dto.getObservaciones() != null) {
            orden.setObservaciones(dto.getObservaciones());
        }
        if (dto.getEstado() != null) {
            String estadoUpper = dto.getEstado().trim().toUpperCase();
            if (!List.of("PENDIENTE", "REALIZADO", "ANULADA").contains(estadoUpper)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido (PENDIENTE, REALIZADO o ANULADA)");
            }
            orden.setEstado(estadoUpper);
        } else if (orden.getResultado() != null && !orden.getResultado().trim().isEmpty()) {
            orden.setEstado("REALIZADO");
        }

        orden = ordenExamenRepository.save(orden);
        return ordenExamenMapper.toDTO(orden);
    }

    @Transactional(readOnly = true)
    public OrdenesMedicasClienteDTO obtenerMisOrdenesMedicas(String currentUsername) {
        Usuario usuario = usuarioRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un perfil de cliente asociado a este usuario"));

        List<Mascota> mascotas = mascotaRepository.findByPropietarioId(cliente.getId());
        if (mascotas.isEmpty()) {
            return new OrdenesMedicasClienteDTO(List.of(), List.of(), List.of());
        }

        List<Long> mascotaIds = mascotas.stream().map(Mascota::getId).toList();
        Map<Long, String> petNames = mascotas.stream().collect(Collectors.toMap(Mascota::getId, Mascota::getNombre));

        List<Consulta> consultas = consultaRepository.findByMascotaIdIn(mascotaIds);
        List<Long> consultaIds = consultas.stream().map(Consulta::getId).toList();
        Map<Long, Long> consultaToMascotaId = consultas.stream().collect(Collectors.toMap(
                Consulta::getId,
                c -> c.getMascota() != null ? c.getMascota().getId() : null,
                (k1, k2) -> k1
        ));

        List<RecetaMedica> recetas = consultaIds.isEmpty() ? List.of() : recetaMedicaRepository.findByConsultaIdInOrderByFechaDesc(consultaIds);
        List<ReferenciaMedica> referencias = consultaIds.isEmpty() ? List.of() : referenciaMedicaRepository.findByConsultaIdInOrderByFechaDesc(consultaIds);

        List<OrdenExamen> examenesConsulta = consultaIds.isEmpty() ? List.of() : ordenExamenRepository.findByConsultaIdIn(consultaIds);
        List<OrdenExamen> examenesCita = ordenExamenRepository.findByCitaMascotaIdIn(mascotaIds);

        Map<Long, OrdenExamen> uniqueExamenes = new LinkedHashMap<>();
        for (OrdenExamen e : examenesConsulta) {
            uniqueExamenes.put(e.getId(), e);
        }
        for (OrdenExamen e : examenesCita) {
            uniqueExamenes.putIfAbsent(e.getId(), e);
        }
        List<OrdenExamen> examenes = new ArrayList<>(uniqueExamenes.values());
        examenes.sort((a, b) -> {
            if (a.getFecha() == null && b.getFecha() == null) return 0;
            if (a.getFecha() == null) return 1;
            if (b.getFecha() == null) return -1;
            return b.getFecha().compareTo(a.getFecha());
        });

        List<RecetaMedicaDTO> recetasDTO = recetas.stream().map(r -> {
            RecetaMedicaDTO dto = recetaMedicaMapper.toDTO(r);
            Long mId = (r.getConsulta() != null) ? consultaToMascotaId.get(r.getConsulta().getId()) : null;
            if (mId != null) {
                dto.setMascotaNombre(petNames.get(mId));
            }
            return dto;
        }).toList();

        List<ReferenciaMedicaDTO> referenciasDTO = referencias.stream().map(ref -> {
            ReferenciaMedicaDTO dto = referenciaMedicaMapper.toDTO(ref);
            Long mId = (ref.getConsulta() != null) ? consultaToMascotaId.get(ref.getConsulta().getId()) : null;
            if (mId != null) {
                dto.setMascotaNombre(petNames.get(mId));
            }
            return dto;
        }).toList();

        List<OrdenExamenDTO> examenesDTO = examenes.stream().map(e -> {
            OrdenExamenDTO dto = ordenExamenMapper.toDTO(e);
            Long mId = null;
            if (e.getConsulta() != null) {
                mId = consultaToMascotaId.get(e.getConsulta().getId());
            } else if (e.getCita() != null && e.getCita().getMascota() != null) {
                mId = e.getCita().getMascota().getId();
            }
            if (mId != null) {
                dto.setMascotaNombre(petNames.get(mId));
            }
            return dto;
        }).toList();

        return OrdenesMedicasClienteDTO.builder()
                .recetas(recetasDTO)
                .examenes(examenesDTO)
                .referencias(referenciasDTO)
                .build();
    }

    private Mascota verificarMascotaCliente(Long mascotaId, String currentUsername, String currentRole) {
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mascota no encontrada"));
        if (isClienteRole(currentRole)) {
            Usuario usuario = (currentUsername != null) ? usuarioRepository.findByUsername(currentUsername).orElse(null) : null;
            Cliente cliente = (usuario != null) ? clienteRepository.findByUsuarioId(usuario.getId()).orElse(null) : null;
            if (cliente == null || mascota.getPropietario() == null || !mascota.getPropietario().getId().equals(cliente.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver esta mascota");
            }
        }
        return mascota;
    }

    private boolean isClienteRole(String role) {
        if (role == null) return false;
        return "Cliente".equalsIgnoreCase(role) || "ROLE_CLIENTE".equalsIgnoreCase(role) || "CLIENTE".equalsIgnoreCase(role);
    }
}
