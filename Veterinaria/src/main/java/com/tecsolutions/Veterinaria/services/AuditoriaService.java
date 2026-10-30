package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.AuditoriaDTO;
import com.tecsolutions.Veterinaria.mapper.AuditoriaMapper;
import com.tecsolutions.Veterinaria.model.Auditoria;
import com.tecsolutions.Veterinaria.model.Usuario;
import com.tecsolutions.Veterinaria.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final AuditoriaMapper auditoriaMapper;

    public AuditoriaService(AuditoriaRepository auditoriaRepository, AuditoriaMapper auditoriaMapper) {
        this.auditoriaRepository = auditoriaRepository;
        this.auditoriaMapper = auditoriaMapper;
    }

    @Transactional(readOnly = true)
    public List<AuditoriaDTO> listarAuditoria() {
        return auditoriaRepository.findTop100ByOrderByTimestampDesc().stream()
                .map(auditoriaMapper::toDTO)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Auditoria registrarAccion(Usuario usuario, String accion, String endpoint, String resourceId, String detalle) {
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setAccion(accion);
        auditoria.setEndpoint(endpoint);
        auditoria.setResourceId(resourceId);
        auditoria.setTimestamp(LocalDateTime.now());
        auditoria.setDetalle(detalle);
        return auditoriaRepository.save(auditoria);
    }
}
