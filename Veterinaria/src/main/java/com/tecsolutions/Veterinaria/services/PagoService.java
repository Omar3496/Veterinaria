package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.PagoCreateDTO;
import com.tecsolutions.Veterinaria.dto.PagoDTO;
import com.tecsolutions.Veterinaria.mapper.PagoMapper;
import com.tecsolutions.Veterinaria.model.*;
import com.tecsolutions.Veterinaria.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class PagoService {

    private final PagoRepository pagoRepository;
    private final CitaRepository citaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComprobanteSeqRepository comprobanteSeqRepository;
    private final VentaRepository ventaRepository;
    private final ConfiguracionService configuracionService;
    private final PagoMapper pagoMapper;

    public PagoService(
            PagoRepository pagoRepository,
            CitaRepository citaRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            ComprobanteSeqRepository comprobanteSeqRepository,
            VentaRepository ventaRepository,
            ConfiguracionService configuracionService,
            PagoMapper pagoMapper) {
        this.pagoRepository = pagoRepository;
        this.citaRepository = citaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.comprobanteSeqRepository = comprobanteSeqRepository;
        this.ventaRepository = ventaRepository;
        this.configuracionService = configuracionService;
        this.pagoMapper = pagoMapper;
    }

    public PagoDTO crearPagoCita(Long citaId, PagoCreateDTO dto, String currentUsername, String currentRole) {
        Cita dbCita = citaRepository.findById(citaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada"));

        if (isClienteRole(currentRole)) {
            Usuario usuario = usuarioRepository.findByUsername(currentUsername).orElse(null);
            Cliente cliente = (usuario != null) ? clienteRepository.findByUsuarioId(usuario.getId()).orElse(null) : null;
            if (cliente == null || dbCita.getMascota() == null || dbCita.getMascota().getPropietario() == null
                    || !dbCita.getMascota().getPropietario().getId().equals(cliente.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para pagar esta cita");
            }
        }

        Map<String, Object> cfg = configuracionService.obtenerConfigPago();

        Double monto = (dto != null) ? dto.getMonto() : null;
        if (monto == null) {
            if ("Examen auxiliar".equalsIgnoreCase(dbCita.getTipo()) && dbCita.getExamenAuxiliar() != null) {
                monto = dbCita.getExamenAuxiliar().getPrecio();
                if (monto == null) {
                    monto = ((Number) cfg.getOrDefault("costo_cita", 0.0)).doubleValue();
                }
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> tipos = (Map<String, Object>) cfg.get("tipos");
                if (tipos != null && tipos.containsKey(dbCita.getTipo())) {
                    monto = ((Number) tipos.get(dbCita.getTipo())).doubleValue();
                } else {
                    monto = ((Number) cfg.getOrDefault("costo_cita", 0.0)).doubleValue();
                }
            }
        }

        String metodo = (dto != null && dto.getMetodo() != null && !dto.getMetodo().isBlank())
                ? dto.getMetodo().toUpperCase() : "YAPE";

        @SuppressWarnings("unchecked")
        List<?> metodosCfg = (List<?>) cfg.get("metodos");
        boolean metodoValido = metodosCfg != null && metodosCfg.stream()
                .anyMatch(m -> m != null && m.toString().equalsIgnoreCase(metodo));

        if (!metodoValido) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Método de pago no soportado: " + (dto != null ? dto.getMetodo() : ""));
        }

        String tipoComprobante = (dto != null && dto.getTipoComprobante() != null && !dto.getTipoComprobante().isBlank())
                ? dto.getTipoComprobante() : "Boleta";
        String documentoFiscal = (dto != null) ? dto.getDocumentoFiscal() : null;

        String referencia = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Pago dbPago = new Pago();
        dbPago.setCita(dbCita);
        dbPago.setMonto(monto);
        dbPago.setMetodo(metodo);
        dbPago.setEstado("PENDIENTE");
        dbPago.setReferencia(referencia);
        dbPago.setTipoComprobante(tipoComprobante);
        dbPago.setDocumentoFiscal(documentoFiscal);
        dbPago.setFecha(LocalDateTime.now());

        Pago guardado = pagoRepository.save(dbPago);
        return pagoMapper.toDTO(guardado);
    }

    public PagoDTO procesarPago(Long pagoId, Map<String, Object> resultado, String currentUsername, String currentRole) {
        Pago dbPago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));

        if (isClienteRole(currentRole)) {
            Usuario usuario = usuarioRepository.findByUsername(currentUsername).orElse(null);
            Cliente cliente = (usuario != null) ? clienteRepository.findByUsuarioId(usuario.getId()).orElse(null) : null;
            if (cliente == null || dbPago.getCita() == null || dbPago.getCita().getMascota() == null
                    || dbPago.getCita().getMascota().getPropietario() == null
                    || !dbPago.getCita().getMascota().getPropietario().getId().equals(cliente.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para procesar este pago");
            }
        }

        boolean success = false;
        if (resultado == null) {
            success = true;
        } else if (resultado.containsKey("success")) {
            Object s = resultado.get("success");
            if (s instanceof Boolean b) {
                success = b;
            } else if (s != null) {
                success = Boolean.parseBoolean(s.toString());
            }
        }

        if (success) {
            dbPago.setEstado("COMPLETADO");
            dbPago.setDetalle("Pago simulado completado");

            if (dbPago.getCita() != null && dbPago.getCita().getEstado() == EstadoCita.PENDIENTE) {
                dbPago.getCita().setEstado(EstadoCita.PROGRAMADA);
            }

            Cliente cliente = (dbPago.getCita() != null && dbPago.getCita().getMascota() != null)
                    ? dbPago.getCita().getMascota().getPropietario() : null;

            String tipoComprobante = (dbPago.getTipoComprobante() != null && !dbPago.getTipoComprobante().isBlank())
                    ? dbPago.getTipoComprobante() : "Boleta";
            String documentoFiscal = dbPago.getDocumentoFiscal();
            if ((documentoFiscal == null || documentoFiscal.isBlank()) && cliente != null) {
                documentoFiscal = cliente.getDni();
            }

            if ("Factura".equalsIgnoreCase(tipoComprobante)) {
                if (documentoFiscal == null || !documentoFiscal.matches("\\d{11}")) {
                    documentoFiscal = (cliente != null && cliente.getDni() != null) ? cliente.getDni() : "00000000";
                    tipoComprobante = "Boleta";
                }
            } else {
                if (documentoFiscal == null || documentoFiscal.isBlank()) {
                    documentoFiscal = (cliente != null && cliente.getDni() != null) ? cliente.getDni() : "00000000";
                }
            }

            String serie = "Factura".equalsIgnoreCase(tipoComprobante) ? "FA64" : "BA64";
            ComprobanteSeq fila = comprobanteSeqRepository.findBySerie(serie).orElse(null);
            if (fila == null) {
                int ultimo = 0;
                for (Venta v : ventaRepository.findBySerie(serie)) {
                    if (v.getNumero() != null && v.getNumero().matches("\\d+")) {
                        ultimo = Math.max(ultimo, Integer.parseInt(v.getNumero()));
                    }
                }
                for (Pago p : pagoRepository.findBySerie(serie)) {
                    if (p.getNumero() != null && p.getNumero().matches("\\d+")) {
                        ultimo = Math.max(ultimo, Integer.parseInt(p.getNumero()));
                    }
                }
                fila = new ComprobanteSeq(null, serie, ultimo);
                comprobanteSeqRepository.saveAndFlush(fila);
            }

            fila.setUltimoNumero(fila.getUltimoNumero() + 1);
            comprobanteSeqRepository.save(fila);
            String numero = String.format("%010d", fila.getUltimoNumero());

            dbPago.setTipoComprobante(tipoComprobante);
            dbPago.setDocumentoFiscal(documentoFiscal);
            dbPago.setSerie(serie);
            dbPago.setNumero(numero);
        } else {
            dbPago.setEstado("FALLIDO");
            dbPago.setDetalle("Pago simulado fallido");
        }

        Pago guardado = pagoRepository.save(dbPago);
        return pagoMapper.toDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<PagoDTO> listarMisPagos(String currentUsername) {
        Usuario usuario = usuarioRepository.findByUsername(currentUsername).orElse(null);
        Cliente cliente = (usuario != null) ? clienteRepository.findByUsuarioId(usuario.getId()).orElse(null) : null;
        if (cliente == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe un perfil de cliente asociado a este usuario");
        }

        List<Long> citaIds = citaRepository.findByMascotaPropietarioId(cliente.getId()).stream()
                .map(Cita::getId)
                .toList();

        if (citaIds.isEmpty()) {
            return Collections.emptyList();
        }

        return pagoRepository.findByCitaIdInOrderByFechaDesc(citaIds).stream()
                .map(pagoMapper::toDTO)
                .toList();
    }

    private boolean isClienteRole(String role) {
        if (role == null) return false;
        return role.equalsIgnoreCase("Cliente")
                || role.equalsIgnoreCase("ROLE_Cliente")
                || role.equalsIgnoreCase("CLIENTE")
                || role.equalsIgnoreCase("ROLE_CLIENTE");
    }
}
