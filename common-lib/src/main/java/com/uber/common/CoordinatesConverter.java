package com.uber.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CoordinatesConverter implements AttributeConverter<Coordinates, String> {

    @Override
    public String convertToDatabaseColumn(Coordinates attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getLatitude() + "," + attribute.getLongitude();
    }

    @Override
    public Coordinates convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        String[] parts = dbData.split(",");
        return new Coordinates(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }
}
