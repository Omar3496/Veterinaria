package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoCitaConverter implements AttributeConverter<EstadoCita, String> {

    @Override
    public String convertToDatabaseColumn(EstadoCita attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public EstadoCita convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EstadoCita.fromValue(dbData);
    }
}
