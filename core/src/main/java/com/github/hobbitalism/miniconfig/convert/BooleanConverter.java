package com.github.hobbitalism.miniconfig.convert;

public final class BooleanConverter implements TypeConverter<Boolean> {
    @Override
    public Boolean deserialize(Object raw) {
        if (raw instanceof Boolean) return (Boolean) raw;
        String s = raw.toString().trim();
        if (s.length() == 1) {
            char c = s.charAt(0);
            if (c == '1' || c == 't' || c == 'T') return true;
            if (c == '0' || c == 'f' || c == 'F') return false;
        }
        if ("true".equals(s) || "TRUE".equals(s)) return true;
        if ("false".equals(s) || "FALSE".equals(s)) return false;
        throw new ConversionException("Cannot convert to boolean: '" + raw + "'");
    }

    @Override
    public Object serialize(Boolean value) {
        return value;
    }
}
