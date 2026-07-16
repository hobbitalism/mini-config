package com.github.hobbitalism.miniconfig.convert;

public final class FloatConverter implements TypeConverter<Float> {
    @Override
    public Float deserialize(Object raw) {
        if (raw instanceof Number) return ((Number) raw).floatValue();
        try {
            return Float.parseFloat(raw.toString().trim());
        } catch (NumberFormatException e) {
            throw new ConversionException("Cannot convert to float: '" + raw + "'", e);
        }
    }

    @Override
    public Object serialize(Float value) {
        return value;
    }
}
