package com.github.hobbitalism.miniconfig.convert;

public final class IntegerConverter implements TypeConverter<Integer> {
    @Override
    public Integer deserialize(Object raw) {
        if (raw instanceof Number) return ((Number) raw).intValue();
        try {
            return Integer.parseInt(raw.toString().trim());
        } catch (NumberFormatException e) {
            throw new ConversionException("Cannot convert to int: '" + raw + "'", e);
        }
    }

    @Override
    public Object serialize(Integer value) {
        return value;
    }
}
