package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.HorarioConfigDTO;
import com.tecsolutions.Veterinaria.model.Configuracion;
import com.tecsolutions.Veterinaria.repository.ConfiguracionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ConfiguracionService {

    public static final String CLAVE_HORARIOS = "horarios";
    public static final String CLAVE_PAGO_CONFIG = "pago_config";

    private final ConfiguracionRepository configuracionRepository;
    private final ObjectMapper objectMapper;

    public ConfiguracionService(ConfiguracionRepository configuracionRepository, ObjectMapper objectMapper) {
        this.configuracionRepository = configuracionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public HorarioConfigDTO obtenerHorarios() {
        Configuracion row = configuracionRepository.findById(CLAVE_HORARIOS).orElse(null);
        if (row == null || row.getValor() == null || row.getValor().isBlank()) {
            return HorarioConfigDTO.builder().build();
        }
        try {
            return objectMapper.readValue(row.getValor(), HorarioConfigDTO.class);
        } catch (Exception e) {
            return HorarioConfigDTO.builder().build();
        }
    }

    public HorarioConfigDTO actualizarHorarios(HorarioConfigDTO dto) {
        if (dto.getDias() == null || dto.getDias().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Día inválido (usa 1=Lunes ... 7=Domingo)");
        }
        for (Integer d : dto.getDias()) {
            if (d == null || d < 1 || d > 7) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Día inválido (usa 1=Lunes ... 7=Domingo)");
            }
        }
        if (dto.getHoraInicio() == null || dto.getHoraFin() == null || dto.getHoraInicio().compareTo(dto.getHoraFin()) >= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La hora de inicio debe ser anterior a la hora de fin");
        }
        if (dto.getDuracionMin() == null || (!dto.getDuracionMin().equals(15) && !dto.getDuracionMin().equals(30) && !dto.getDuracionMin().equals(60))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La duración del turno debe ser 15, 30 o 60 minutos");
        }

        try {
            String json = objectMapper.writeValueAsString(dto);
            Configuracion config = configuracionRepository.findById(CLAVE_HORARIOS)
                    .orElse(new Configuracion(CLAVE_HORARIOS, null));
            config.setValor(json);
            configuracionRepository.save(config);
            return obtenerHorarios();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar horarios: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerConfigPago() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("pago_requerido", true);
        defaults.put("costo_cita", 30.0);
        defaults.put("metodos", List.of("YAPE", "PLIN", "TARJETA", "EFECTIVO"));
        defaults.put("tipos", Map.of(
                "Consulta médica", 30.0,
                "Control / Seguimiento", 25.0,
                "Vacunación", 35.0,
                "Baño / Aseo", 20.0,
                "Examen auxiliar", 0.0,
                "Cirugía", 100.0
        ));

        Configuracion row = configuracionRepository.findById(CLAVE_PAGO_CONFIG).orElse(null);
        if (row == null || row.getValor() == null || row.getValor().isBlank()) {
            return defaults;
        }

        try {
            Map<String, Object> data = objectMapper.readValue(row.getValor(), Map.class);
            Map<String, Object> merged = new LinkedHashMap<>(defaults);
            if (data != null) {
                merged.putAll(data);
                if (!merged.containsKey("metodos") || merged.get("metodos") == null) {
                    merged.put("metodos", defaults.get("metodos"));
                }
                if (!merged.containsKey("tipos") || merged.get("tipos") == null) {
                    merged.put("tipos", defaults.get("tipos"));
                }
            }
            return merged;
        } catch (Exception e) {
            return defaults;
        }
    }

    public Map<String, Object> actualizarConfigPago(Map<String, Object> payload) {
        try {
            Map<String, Object> current = new LinkedHashMap<>(obtenerConfigPago());
            if (payload != null) {
                current.putAll(payload);
            }
            String json = objectMapper.writeValueAsString(current);
            Configuracion config = configuracionRepository.findById(CLAVE_PAGO_CONFIG)
                    .orElse(new Configuracion(CLAVE_PAGO_CONFIG, null));
            config.setValor(json);
            configuracionRepository.save(config);
            return obtenerConfigPago();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar configuración de pago: " + e.getMessage());
        }
    }
}
