package com.github.hobbitalism.miniconfig.convert;

public final class DoubleConverter implements TypeConverter<Double> {
    @Override
    public Double deserialize(Object raw) {
        if (raw instanceof Number) return ((Number) raw).doubleValue();
        try {
            return Double.parseDouble(raw.toString().trim());
        } catch (NumberFormatException e) {
            throw new ConversionException("Cannot convert to double: '" + raw + "'", e);
        }
    }

    @Override
    public Object serialize(Double value) {
        return value;
    }
}
