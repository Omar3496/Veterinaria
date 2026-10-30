package com.tecsolutions.Veterinaria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VentaEstado {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    ENTREGADA("Entregada"),
    CANCELADA("Cancelada"),
    ANULACION_SOLICITADA("Anulación solicitada");

    private final String value;

    VentaEstado(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static VentaEstado fromValue(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        for (VentaEstado estado : VentaEstado.values()) {
            if (estado.value.equalsIgnoreCase(trimmed) || estado.name().equalsIgnoreCase(trimmed)) {
                return estado;
            }
        }
        String normalized = trimmed.replace("ó", "o").replace("Ó", "O");
        for (VentaEstado estado : VentaEstado.values()) {
            String estadoNorm = estado.value.replace("ó", "o").replace("Ó", "O");
            if (estadoNorm.equalsIgnoreCase(normalized)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de venta inválido: " + text);
    }
}
