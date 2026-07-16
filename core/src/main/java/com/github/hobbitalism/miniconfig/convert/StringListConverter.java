package com.github.hobbitalism.miniconfig.convert;

import java.util.ArrayList;
import java.util.List;

public final class StringListConverter implements TypeConverter<List<String>> {
    @Override
    @SuppressWarnings("unchecked")
    public List<String> deserialize(Object raw) {
        if (raw instanceof List) {
            List<?> list = (List<?>) raw;
            List<String> result = new ArrayList<>(list.size());
            for (Object o : list) {
                result.add(o == null ? null : o.toString());
            }
            return result;
        }
        return List.of(raw.toString());
    }

    @Override
    public Object serialize(List<String> value) {
        return value;
    }
}
