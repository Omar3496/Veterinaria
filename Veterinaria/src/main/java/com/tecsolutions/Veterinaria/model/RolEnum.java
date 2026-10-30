package com.tecsolutions.Veterinaria.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RolEnum {
    ADMIN("Administrador"),
    VETERINARIO("Veterinario"),
    RECEPCIONISTA("Recepcionista"),
    CLIENTE("Cliente");

    private final String value;

    RolEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RolEnum fromValue(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        for (RolEnum rol : RolEnum.values()) {
            if (rol.value.equalsIgnoreCase(trimmed) || rol.name().equalsIgnoreCase(trimmed)) {
                return rol;
            }
        }
        throw new IllegalArgumentException("Rol inválido: " + text);
    }
}
