package com.github.hobbitalism.miniconfig.convert;

public final class LongConverter implements TypeConverter<Long> {
    @Override
    public Long deserialize(Object raw) {
        if (raw instanceof Number) return ((Number) raw).longValue();
        try {
            return Long.parseLong(raw.toString().trim());
        } catch (NumberFormatException e) {
            throw new ConversionException("Cannot convert to long: '" + raw + "'", e);
        }
    }

    @Override
    public Object serialize(Long value) {
        return value;
    }
}
