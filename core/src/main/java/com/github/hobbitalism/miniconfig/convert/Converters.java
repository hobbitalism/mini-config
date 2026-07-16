package com.github.hobbitalism.miniconfig.convert;

import java.util.List;
import java.util.UUID;

/**
 * Built-in {@link TypeConverter} implementations for common Java types.
 *
 * <p>All instances are stateless singletons exposed as public constants.
 * They are pre-registered by {@link com.github.hobbitalism.miniconfig.container.ConverterRegistry}.
 */
public final class Converters {

    private Converters() {}

    /** Passes strings through as-is; calls {@code toString()} on anything else. */
    public static final TypeConverter<String> STRING = new StringConverter();

    /** Converts to {@code int} / {@link Integer}. */
    public static final TypeConverter<Integer> INTEGER = new IntegerConverter();

    /** Converts to {@code long} / {@link Long}. */
    public static final TypeConverter<Long> LONG = new LongConverter();

    /** Converts to {@code double} / {@link Double}. */
    public static final TypeConverter<Double> DOUBLE = new DoubleConverter();

    /** Converts to {@code float} / {@link Float}. */
    public static final TypeConverter<Float> FLOAT = new FloatConverter();

    /** Converts to {@code boolean} / {@link Boolean}. */
    public static final TypeConverter<Boolean> BOOLEAN = new BooleanConverter();

    /** Converts to {@link UUID} from a standard UUID string. */
    public static final TypeConverter<UUID> UUID_CONVERTER = new UUIDConverter();

    /**
     * Converts to {@code List<String>}.
     * If the raw value is already a {@link List}, each element is converted via
     * {@link Object#toString()}. A plain string is treated as a single-element list.
     */
    public static final TypeConverter<List<String>> STRING_LIST = new StringListConverter();
}
