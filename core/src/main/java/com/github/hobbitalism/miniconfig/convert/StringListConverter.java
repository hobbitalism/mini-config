package com.github.hobbitalism.miniconfig.convert;

import java.util.List;
import java.util.stream.Collectors;

public final class StringListConverter implements TypeConverter<List<String>> {
    @Override
    @SuppressWarnings("unchecked")
    public List<String> deserialize(Object raw) {
        if (raw instanceof List) {
            return ((List<?>) raw).stream()
                    .map(o -> o == null ? null : o.toString())
                    .collect(Collectors.toList());
        }
        return List.of(raw.toString());
    }

    @Override
    public Object serialize(List<String> value) {
        return value;
    }
}
