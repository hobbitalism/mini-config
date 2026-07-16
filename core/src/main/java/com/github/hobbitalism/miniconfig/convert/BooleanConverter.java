package com.github.hobbitalism.miniconfig.convert;

public final class BooleanConverter implements TypeConverter<Boolean> {
    @Override
    public Boolean deserialize(Object raw) {
        if (raw instanceof Boolean) return (Boolean) raw;
        String s = raw.toString().trim();
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("t") || s.equals("1")) return true;
        if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("f") || s.equals("0")) return false;
        throw new ConversionException("Cannot convert to boolean: '" + raw + "'");
    }

    @Override
    public Object serialize(Boolean value) {
        return value;
    }
}
