package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoAlmacenConverter implements AttributeConverter<EstadoAlmacen, String> {

    @Override
    public String convertToDatabaseColumn(EstadoAlmacen attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public EstadoAlmacen convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EstadoAlmacen.fromValue(dbData);
    }
}
