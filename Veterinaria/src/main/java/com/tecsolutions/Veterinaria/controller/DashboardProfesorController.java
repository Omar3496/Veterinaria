package com.tecsolutions.Veterinaria.controller;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tecsolutions.Veterinaria.model.Mascota;
import com.tecsolutions.Veterinaria.model.Producto;
import com.tecsolutions.Veterinaria.repository.MascotaRepository;
import com.tecsolutions.Veterinaria.repository.ProductoRepository;

@RestController
public class DashboardProfesorController {

    private final MascotaRepository mascotaRepository;
    private final ProductoRepository productoRepository;

    public DashboardProfesorController(MascotaRepository mascotaRepository, ProductoRepository productoRepository) {
        this.mascotaRepository = mascotaRepository;
        this.productoRepository = productoRepository;
    }

    // =========================================================================
    // 1. DASHBOARD PRINCIPAL CONSOLIDADO (Texto plano similar a la diapositiva)
    // =========================================================================
    @GetMapping(value = { "/dashboard", "/dashboard-critico" }, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> verEstadisticas() {
        Long totalPerros = mascotaRepository.contarPorEspecie("Perro");
        Long totalGatos = mascotaRepository.contarPorEspecie("Gato");

        List<Mascota> masLongevas = mascotaRepository.obtenerMascotaMasLongeva();
        String detalleLongevo = "No hay registros";
        if (!masLongevas.isEmpty()) {
            Mascota m = masLongevas.get(0);
            int edadAprox = m.getFechaNacimiento() != null
                    ? Period.between(m.getFechaNacimiento(), LocalDate.now()).getYears()
                    : 0;
            detalleLongevo = m.getNombre() + " (" + m.getEspecie() + " - " + m.getRaza() + ") con " + edadAprox
                    + " años";
        }

        Integer stockAmoxicilina = productoRepository.obtenerStockPorMedicamento("Amoxicilina");
        Integer stockNaproxeno = productoRepository.obtenerStockPorMedicamento("Naproxeno");
        Integer stockParacetamol = productoRepository.obtenerStockPorMedicamento("Paracetamol");
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
        sb.append(" - Stock de Naproxeno: ").append(stockNaproxeno).append(" unidades\n");
        sb.append(" - Stock de Paracetamol: ").append(stockParacetamol).append(" unidades\n");
        sb.append(" - Stock total de Vacunas: ").append(stockVacunas).append(" unidades\n\n");
        sb.append(" - Medicamentos con stock crítico (<= 10 unidades):\n");

        for (Producto p : criticos) {
            sb.append("   * [").append(p.getSku()).append("] ").append(p.getNombre())
                    .append(" -> Stock actual: ").append(p.getStock()).append(" unidades\n");
        }

        return new ResponseEntity<>(sb.toString(), HttpStatus.OK);
    }

    // =========================================================================
    // 2. ENDPOINTS INDIVIDUALES PARA CADA @Query SOLICITADO (Formato JSON)
    // =========================================================================

    // Query 1: Contar por especie (ej: /dashboard/mascotas/contar?especie=Perro)
    @GetMapping("/dashboard/mascotas/contar")
    public ResponseEntity<Map<String, Object>> contarPorEspecie(
            @RequestParam(defaultValue = "Perro") String especie) {
        Long total = mascotaRepository.contarPorEspecie(especie);
        return ResponseEntity.ok(Map.of(
                "especie", especie,
                "total", total
        ));
    }

    // Query 2: Mascota más longeva completa (ej: /dashboard/mascotas/mas-longeva)
    @GetMapping("/dashboard/mascotas/mas-longeva")
    public ResponseEntity<?> obtenerMascotaMasLongeva() {
        List<Mascota> lista = mascotaRepository.obtenerMascotaMasLongeva();
        if (lista.isEmpty()) {
            return ResponseEntity.ok(Map.of("mensaje", "No hay mascotas con fecha de nacimiento registrada"));
        }
        Mascota m = lista.get(0);
        int edad = m.getFechaNacimiento() != null
                ? Period.between(m.getFechaNacimiento(), LocalDate.now()).getYears()
                : 0;
        return ResponseEntity.ok(Map.of(
                "id", m.getId(),
                "nombre", m.getNombre(),
                "especie", m.getEspecie(),
                "raza", m.getRaza() != null ? m.getRaza() : "",
                "fecha_nacimiento", m.getFechaNacimiento() != null ? m.getFechaNacimiento().toString() : "",
                "edad_anios", edad,
                "descripcion", m.getNombre() + " (" + m.getEspecie() + ") es la mascota más longeva con " + edad + " años"
        ));
    }

    // Query 3: Stock de un medicamento específico (ej: /dashboard/medicamentos/stock?nombre=Naproxeno)
    @GetMapping("/dashboard/medicamentos/stock")
    public ResponseEntity<Map<String, Object>> obtenerStockPorNombre(
            @RequestParam(defaultValue = "Amoxicilina") String nombre) {
        Integer stockTotal = productoRepository.obtenerStockPorMedicamento(nombre);
        return ResponseEntity.ok(Map.of(
                "medicamento", nombre,
                "stock_total", stockTotal
        ));
    }

    // Query 4: Medicamentos con stock crítico (ej: /dashboard/medicamentos/criticos?limite=10)
    @GetMapping("/dashboard/medicamentos/criticos")
    public ResponseEntity<List<Map<String, Object>>> obtenerMedicamentosCriticos(
            @RequestParam(defaultValue = "10") Integer limite) {
        List<Producto> lista = productoRepository.obtenerMedicamentosCriticos(limite);
        List<Map<String, Object>> respuesta = lista.stream().map(p -> Map.<String, Object>of(
                "sku", p.getSku(),
                "nombre", p.getNombre(),
                "stock", p.getStock(),
                "stock_minimo", p.getStockMinimo() != null ? p.getStockMinimo() : 0
        )).toList();
        return ResponseEntity.ok(respuesta);
    }

    // Query 5: Búsqueda con parámetros (ej: /dashboard/medicamentos/buscar?nombre=Naproxeno&cantidad=10)
    @GetMapping("/dashboard/medicamentos/buscar")
    public ResponseEntity<List<Map<String, Object>>> buscarMedicamento(
            @RequestParam(defaultValue = "Naproxeno") String nombre,
            @RequestParam(defaultValue = "10") Integer cantidad) {
        List<Producto> lista = productoRepository.buscarPorNombreYStock(nombre, cantidad);
        List<Map<String, Object>> respuesta = lista.stream().map(p -> Map.<String, Object>of(
                "sku", p.getSku(),
                "nombre", p.getNombre(),
                "stock", p.getStock(),
                "precio", p.getPrecio() != null ? p.getPrecio() : 0.0
        )).toList();
        return ResponseEntity.ok(respuesta);
    }

    // Formato texto para búsqueda rápida en navegador
    @GetMapping(value = { "/dashboard/buscar", "/dashboard-critico/buscar" }, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> buscarMedicamentoTexto(
            @RequestParam(defaultValue = "Amoxicilina") String nombre,
            @RequestParam(defaultValue = "15") Integer stockMaximo) {

        List<Producto> resultados = productoRepository.buscarPorNombreYStock(nombre, stockMaximo);
        StringBuilder sb = new StringBuilder();
        sb.append("=== RESULTADOS DE BÚSQUEDA ===\n");
        sb.append("Criterios -> Nombre contiene: '").append(nombre).append("' y Stock <= ").append(stockMaximo)
                .append("\n\n");

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
