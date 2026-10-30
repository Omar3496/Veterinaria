package com.tecsolutions.Veterinaria.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoConfigDTO {

    @JsonProperty("pago_requerido")
    @Builder.Default
    private Boolean pagoRequerido = true;

    @JsonProperty("costo_cita")
    @Builder.Default
    private Double costoCita = 30.0;

    @Builder.Default
    private List<String> metodos = List.of("YAPE", "PLIN", "TARJETA", "EFECTIVO");

    @Builder.Default
    private Map<String, Double> tipos = Map.of(
            "Consulta médica", 30.0,
            "Control / Seguimiento", 25.0,
            "Vacunación", 35.0,
            "Baño / Aseo", 20.0,
            "Examen auxiliar", 0.0,
            "Cirugía", 100.0
    );
}
