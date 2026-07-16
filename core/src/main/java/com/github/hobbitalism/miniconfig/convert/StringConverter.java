package com.github.hobbitalism.miniconfig.convert;

public final class StringConverter implements TypeConverter<String> {
    @Override
    public String deserialize(Object raw) {
        return raw.toString();
    }

    @Override
    public Object serialize(String value) {
        return value;
    }
}
