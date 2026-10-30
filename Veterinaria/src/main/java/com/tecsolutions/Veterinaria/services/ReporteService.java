package com.tecsolutions.Veterinaria.services;

import com.tecsolutions.Veterinaria.dto.*;
import com.tecsolutions.Veterinaria.model.Cita;
import com.tecsolutions.Veterinaria.model.DetalleVenta;
import com.tecsolutions.Veterinaria.model.EstadoCita;
import com.tecsolutions.Veterinaria.model.Mascota;
import com.tecsolutions.Veterinaria.model.Venta;
import com.tecsolutions.Veterinaria.repository.CitaRepository;
import com.tecsolutions.Veterinaria.repository.DetalleVentaRepository;
import com.tecsolutions.Veterinaria.repository.MascotaRepository;
import com.tecsolutions.Veterinaria.repository.ProductoRepository;
import com.tecsolutions.Veterinaria.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final CitaRepository citaRepository;
    private final ProductoRepository productoRepository;
    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final MascotaRepository mascotaRepository;

    private static final DateTimeFormatter HORA_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter MES_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public ReporteService(
            CitaRepository citaRepository,
            ProductoRepository productoRepository,
            VentaRepository ventaRepository,
            DetalleVentaRepository detalleVentaRepository,
            MascotaRepository mascotaRepository) {
        this.citaRepository = citaRepository;
        this.productoRepository = productoRepository;
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.mascotaRepository = mascotaRepository;
    }

    public StatsResponseDTO obtenerStats() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        long citasHoy = citaRepository.countByFechaHoraBetween(inicioHoy, finHoy);
        long vacunasPendientes = citaRepository.countByFechaHoraGreaterThanEqualAndEstado(inicioHoy, EstadoCita.PROGRAMADA);
        long alertasStock = productoRepository.countLowStock();

        return new StatsResponseDTO(citasHoy, vacunasPendientes, alertasStock);
    }

    public List<ProximaCitaDTO> obtenerProximasCitas() {
        List<Cita> citas = citaRepository.findAll();
        List<ProximaCitaDTO> result = new ArrayList<>();

        for (Cita cita : citas) {
            String hora = cita.getFechaHora() != null ? cita.getFechaHora().format(HORA_FORMATTER) : "10:00 AM";

            String mascotaStr = "";
            String propietarioStr = "";
            if (cita.getMascota() != null) {
                String esp = cita.getMascota().getEspecie() != null ? cita.getMascota().getEspecie() : "";
                mascotaStr = cita.getMascota().getNombre() + " (" + esp + ")";
                if (cita.getMascota().getPropietario() != null) {
                    propietarioStr = cita.getMascota().getPropietario().getNombre();
                }
            }

            String motivo = (cita.getMotivo() != null && !cita.getMotivo().isBlank())
                    ? cita.getMotivo()
                    : "Consulta General";

            String estado = cita.getEstado() != null ? cita.getEstado().getValue() : "";
            String veterinario = cita.getVeterinarioNombre();

            result.add(ProximaCitaDTO.builder()
                    .id(cita.getId())
                    .hora(hora)
                    .mascota(mascotaStr)
                    .propietario(propietarioStr)
                    .motivo(motivo)
                    .estado(estado)
                    .veterinario(veterinario)
                    .build());
        }

        return result;
    }

    public List<ReporteVentaItemDTO> reporteVentas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Venta> ventas = ventaRepository.findAll();
        Map<String, Double> agregados = new TreeMap<>();

        LocalDateTime start = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime end = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : null;

        for (Venta v : ventas) {
            if (v.getFecha() == null) {
                continue;
            }
            if (start != null && v.getFecha().isBefore(start)) {
                continue;
            }
            if (end != null && v.getFecha().isAfter(end)) {
                continue;
            }

            String fechaStr = v.getFecha().toLocalDate().toString();
            double total = v.getTotal() != null ? v.getTotal() : 0.0;
            agregados.merge(fechaStr, total, Double::sum);
        }

        return agregados.entrySet().stream()
                .map(e -> new ReporteVentaItemDTO(e.getKey(), Math.round(e.getValue() * 100.0) / 100.0))
                .toList();
    }

    public List<ReporteProductoItemDTO> reporteProductos(LocalDate fechaInicio, LocalDate fechaFin) {
        List<DetalleVenta> detalles = detalleVentaRepository.findAll();
        Map<String, Long> sumas = new HashMap<>();

        LocalDateTime start = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime end = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : null;

        for (DetalleVenta d : detalles) {
            if (d.getVenta() == null || d.getVenta().getFecha() == null) {
                continue;
            }
            LocalDateTime f = d.getVenta().getFecha();
            if (start != null && f.isBefore(start)) {
                continue;
            }
            if (end != null && f.isAfter(end)) {
                continue;
            }

            if (d.getProducto() == null || d.getProducto().getNombre() == null) {
                continue;
            }

            String prodNombre = d.getProducto().getNombre();
            long cant = d.getCantidad() != null ? d.getCantidad() : 0L;
            sumas.merge(prodNombre, cant, Long::sum);
        }

        return sumas.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(10)
                .map(e -> new ReporteProductoItemDTO(e.getKey(), e.getValue()))
                .toList();
    }

    public List<ReporteCitaMesItemDTO> reporteCitas(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Cita> citas = citaRepository.findAll();
        Map<String, Map<String, Long>> mesesMap = new TreeMap<>();

        LocalDateTime start = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
        LocalDateTime end = fechaFin != null ? fechaFin.atTime(LocalTime.MAX) : null;

        for (Cita c : citas) {
            if (c.getFechaHora() == null) {
                continue;
            }
            if (start != null && c.getFechaHora().isBefore(start)) {
                continue;
            }
            if (end != null && c.getFechaHora().isAfter(end)) {
                continue;
            }

            String mes = c.getFechaHora().format(MES_FORMATTER);
            String estado = c.getEstado() != null ? c.getEstado().getValue() : null;

            mesesMap.putIfAbsent(mes, new HashMap<>(Map.of(
                    "Completada", 0L,
                    "Cancelada", 0L,
                    "Programada", 0L
            )));

            if (estado != null && mesesMap.get(mes).containsKey(estado)) {
                mesesMap.get(mes).put(estado, mesesMap.get(mes).get(estado) + 1);
            }
        }

        List<ReporteCitaMesItemDTO> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Long>> entry : mesesMap.entrySet()) {
            String mes = entry.getKey();
            Map<String, Long> counts = entry.getValue();
            result.add(new ReporteCitaMesItemDTO(
                    mes,
                    counts.getOrDefault("Completada", 0L),
                    counts.getOrDefault("Cancelada", 0L),
                    counts.getOrDefault("Programada", 0L)
            ));
        }

        return result;
    }

    public List<DemografiaItemDTO> reporteDemografia() {
        List<Mascota> mascotas = mascotaRepository.findAll();
        Map<String, Long> grouped = new LinkedHashMap<>();

        for (Mascota m : mascotas) {
            String especie = m.getEspecie();
            String esp = "Desconocida";
            if (especie != null && !especie.trim().isEmpty()) {
                String trimmed = especie.trim().toLowerCase();
                esp = Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
            }
            grouped.merge(esp, 1L, Long::sum);
        }

        return grouped.entrySet().stream()
                .map(e -> new DemografiaItemDTO(e.getKey(), e.getValue()))
                .toList();
    }
}
