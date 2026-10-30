package com.tecsolutions.Veterinaria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoAlmacen {
    DISPONIBLE("Disponible"),
    NO_DISPONIBLE("No disponible"),
    DANIADO("Dañado"),
    VENCIDO("Vencido");

    private final String value;

    EstadoAlmacen(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static EstadoAlmacen fromValue(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        for (EstadoAlmacen estado : EstadoAlmacen.values()) {
            if (estado.value.equalsIgnoreCase(trimmed) || estado.name().equalsIgnoreCase(trimmed)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de almacén inválido: " + text);
    }
}
