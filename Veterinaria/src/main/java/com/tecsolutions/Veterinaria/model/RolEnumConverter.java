package com.tecsolutions.Veterinaria.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RolEnumConverter implements AttributeConverter<RolEnum, String> {

    @Override
    public String convertToDatabaseColumn(RolEnum attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public RolEnum convertToEntityAttribute(String dbData) {
        return dbData == null ? null : RolEnum.fromValue(dbData);
    }
}
