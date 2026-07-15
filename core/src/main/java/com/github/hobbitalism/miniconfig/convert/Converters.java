package com.github.hobbitalism.miniconfig.convert;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Built-in {@link TypeConverter} implementations for common Java types.
 *
 * <p>All instances are stateless singletons exposed as public constants.
 * They are pre-registered by {@link com.github.hobbitalism.miniconfig.container.ConverterRegistry}.
 */
public final class Converters {

    private Converters() {}

    // -------------------------------------------------------------------------
    // String
    // -------------------------------------------------------------------------

    /** Passes strings through as-is; calls {@code toString()} on anything else. */
    public static final TypeConverter<String> STRING = new TypeConverter<>() {
        @Override public String deserialize(Object raw) { return raw.toString(); }
        @Override public Object serialize(String value) { return value; }
    };

    // -------------------------------------------------------------------------
    // Integer
    // -------------------------------------------------------------------------

    /** Converts to {@code int} / {@link Integer}. */
    public static final TypeConverter<Integer> INTEGER = new TypeConverter<>() {
        @Override
        public Integer deserialize(Object raw) {
            if (raw instanceof Number) return ((Number) raw).intValue();
            try { return Integer.parseInt(raw.toString().trim()); }
            catch (NumberFormatException e) {
                throw new ConversionException("Cannot convert to int: '" + raw + "'", e);
            }
        }
        @Override public Object serialize(Integer value) { return value; }
    };

    // -------------------------------------------------------------------------
    // Long
    // -------------------------------------------------------------------------

    /** Converts to {@code long} / {@link Long}. */
    public static final TypeConverter<Long> LONG = new TypeConverter<>() {
        @Override
        public Long deserialize(Object raw) {
            if (raw instanceof Number) return ((Number) raw).longValue();
            try { return Long.parseLong(raw.toString().trim()); }
            catch (NumberFormatException e) {
                throw new ConversionException("Cannot convert to long: '" + raw + "'", e);
            }
        }
        @Override public Object serialize(Long value) { return value; }
    };

    // -------------------------------------------------------------------------
    // Double
    // -------------------------------------------------------------------------

    /** Converts to {@code double} / {@link Double}. */
    public static final TypeConverter<Double> DOUBLE = new TypeConverter<>() {
        @Override
        public Double deserialize(Object raw) {
            if (raw instanceof Number) return ((Number) raw).doubleValue();
            try { return Double.parseDouble(raw.toString().trim()); }
            catch (NumberFormatException e) {
                throw new ConversionException("Cannot convert to double: '" + raw + "'", e);
            }
        }
        @Override public Object serialize(Double value) { return value; }
    };

    // -------------------------------------------------------------------------
    // Float
    // -------------------------------------------------------------------------

    /** Converts to {@code float} / {@link Float}. */
    public static final TypeConverter<Float> FLOAT = new TypeConverter<>() {
        @Override
        public Float deserialize(Object raw) {
            if (raw instanceof Number) return ((Number) raw).floatValue();
            try { return Float.parseFloat(raw.toString().trim()); }
            catch (NumberFormatException e) {
                throw new ConversionException("Cannot convert to float: '" + raw + "'", e);
            }
        }
        @Override public Object serialize(Float value) { return value; }
    };

    // -------------------------------------------------------------------------
    // Boolean
    // -------------------------------------------------------------------------

    /** Converts to {@code boolean} / {@link Boolean}. */
    public static final TypeConverter<Boolean> BOOLEAN = new TypeConverter<>() {
        @Override
        public Boolean deserialize(Object raw) {
            if (raw instanceof Boolean) return (Boolean) raw;
            String s = raw.toString().trim();
            if (s.equalsIgnoreCase("true"))  return true;
            if (s.equalsIgnoreCase("false")) return false;
            throw new ConversionException("Cannot convert to boolean: '" + raw + "'");
        }
        @Override public Object serialize(Boolean value) { return value; }
    };

    // -------------------------------------------------------------------------
    // UUID
    // -------------------------------------------------------------------------

    /** Converts to {@link UUID} from a standard UUID string. */
    public static final TypeConverter<UUID> UUID_CONVERTER = new TypeConverter<>() {
        @Override
        public UUID deserialize(Object raw) {
            try { return UUID.fromString(raw.toString().trim()); }
            catch (IllegalArgumentException e) {
                throw new ConversionException("Invalid UUID: '" + raw + "'", e);
            }
        }
        @Override public Object serialize(UUID value) { return value.toString(); }
    };

    // -------------------------------------------------------------------------
    // List<String>
    // -------------------------------------------------------------------------

    /**
     * Converts to {@code List<String>}.
     * If the raw value is already a {@link List}, each element is converted via
     * {@link Object#toString()}. A plain string is treated as a single-element list.
     */
    @SuppressWarnings("unchecked")
    public static final TypeConverter<List<String>> STRING_LIST = new TypeConverter<>() {
        @Override
        public List<String> deserialize(Object raw) {
            if (raw instanceof List) {
                return ((List<?>) raw).stream()
                        .map(o -> o == null ? null : o.toString())
                        .collect(Collectors.toList());
            }
            return List.of(raw.toString());
        }
        @Override public Object serialize(List<String> value) { return value; }
    };
}
