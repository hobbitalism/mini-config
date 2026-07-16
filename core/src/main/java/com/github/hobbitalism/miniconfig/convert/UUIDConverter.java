package com.github.hobbitalism.miniconfig.convert;

import java.util.UUID;

public final class UUIDConverter implements TypeConverter<UUID> {
    @Override
    public UUID deserialize(Object raw) {
        try {
            return UUID.fromString(raw.toString().trim());
        } catch (IllegalArgumentException e) {
            throw new ConversionException("Invalid UUID: '" + raw + "'", e);
        }
    }

    @Override
    public Object serialize(UUID value) {
        return value.toString();
    }
}
