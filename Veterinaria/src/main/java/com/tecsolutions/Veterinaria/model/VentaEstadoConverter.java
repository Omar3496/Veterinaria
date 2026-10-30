package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class VentaEstadoConverter implements AttributeConverter<VentaEstado, String> {

    @Override
    public String convertToDatabaseColumn(VentaEstado attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public VentaEstado convertToEntityAttribute(String dbData) {
        return dbData == null ? null : VentaEstado.fromValue(dbData);
    }
}
