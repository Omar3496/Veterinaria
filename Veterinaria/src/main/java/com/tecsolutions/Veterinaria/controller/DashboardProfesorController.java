package com.tecsolutions.Veterinaria.controller;

import com.tecsolutions.Veterinaria.model.Mascota;
import com.tecsolutions.Veterinaria.model.Producto;
import com.tecsolutions.Veterinaria.repository.MascotaRepository;
import com.tecsolutions.Veterinaria.repository.ProductoRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@RestController
public class DashboardProfesorController {

    private final MascotaRepository mascotaRepository;
    private final ProductoRepository productoRepository;

    public DashboardProfesorController(MascotaRepository mascotaRepository, ProductoRepository productoRepository) {
        this.mascotaRepository = mascotaRepository;
        this.productoRepository = productoRepository;
    }

    @GetMapping(value = {"/dashboard", "/dashboard-critico"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> verDashboardCritico() {
        Long totalPerros = mascotaRepository.contarPorEspecie("Perro");
        Long totalGatos = mascotaRepository.contarPorEspecie("Gato");

        List<Mascota> masLongevas = mascotaRepository.obtenerMascotaMasLongeva();
        String detalleLongevo = "No hay registros";
        if (!masLongevas.isEmpty()) {
            Mascota m = masLongevas.get(0);
            int edadAprox = m.getFechaNacimiento() != null 
                    ? Period.between(m.getFechaNacimiento(), LocalDate.now()).getYears() 
                    : 0;
            detalleLongevo = m.getNombre() + " (" + m.getEspecie() + " - " + m.getRaza() + ") con " + edadAprox + " años";
        }

        Integer stockAmoxicilina = productoRepository.obtenerStockPorMedicamento("Amoxicilina");
        Integer stockVacunas = productoRepository.obtenerStockPorMedicamento("Vacuna");
        List<Producto> criticos = productoRepository.obtenerMedicamentosCriticos(10);

        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE DE PUNTOS CRÍTICOS - VETERINARIA SIRBULL PET ===\n\n");
        sb.append("1. POBLACIÓN DE PACIENTES:\n");
        sb.append(" - Perros registrados: ").append(totalPerros).append("\n");
        sb.append(" - Gatos registrados: ").append(totalGatos).append("\n");
        sb.append(" - Mascota más longeva: ").append(detalleLongevo).append("\n\n");

        sb.append("2. PUNTOS CRÍTICOS DE FARMACIA Y MEDICINAS:\n");
        sb.append(" - Stock de Amoxicilina: ").append(stockAmoxicilina).append(" unidades\n");
        sb.append(" - Stock total de Vacunas: ").append(stockVacunas).append(" unidades\n");
        sb.append(" - Medicamentos con stock crítico (<= 10 unidades):\n");

        for (Producto p : criticos) {
            sb.append("   * [").append(p.getSku()).append("] ").append(p.getNombre())
              .append(" -> Stock actual: ").append(p.getStock()).append(" unidades\n");
        }

        return new ResponseEntity<>(sb.toString(), HttpStatus.OK);
    }

    @GetMapping(value = {"/dashboard/buscar", "/dashboard-critico/buscar"}, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> buscarMedicamentoCritico(
            @RequestParam(defaultValue = "Amoxicilina") String nombre,
            @RequestParam(defaultValue = "15") Integer stockMaximo) {

        List<Producto> resultados = productoRepository.buscarPorNombreYStock(nombre, stockMaximo);
        StringBuilder sb = new StringBuilder();
        sb.append("=== RESULTADOS DE BÚSQUEDA ===\n");
        sb.append("Criterios -> Nombre contiene: '").append(nombre).append("' y Stock <= ").append(stockMaximo).append("\n\n");

        if (resultados.isEmpty()) {
            sb.append("No se encontraron medicamentos con esos criterios.");
        } else {
            for (Producto p : resultados) {
                sb.append(" - [").append(p.getSku()).append("] ").append(p.getNombre())
                  .append(" | Stock actual: ").append(p.getStock()).append(" unidades\n");
            }
        }
        return ResponseEntity.ok(sb.toString());
    }
}
