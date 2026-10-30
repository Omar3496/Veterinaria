package com.tecsolutions.Veterinaria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoCita {
    PENDIENTE("Pendiente"),
    PROGRAMADA("Programada"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada");

    private final String value;

    EstadoCita(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EstadoCita fromValue(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        for (EstadoCita estado : EstadoCita.values()) {
            if (estado.value.equalsIgnoreCase(trimmed) || estado.name().equalsIgnoreCase(trimmed)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de cita inválido: " + text);
    }
}
