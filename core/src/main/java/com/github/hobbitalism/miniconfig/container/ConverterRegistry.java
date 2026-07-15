package com.github.hobbitalism.miniconfig.container;

import com.github.hobbitalism.miniconfig.convert.ConversionException;
import com.github.hobbitalism.miniconfig.convert.Converters;
import com.github.hobbitalism.miniconfig.convert.TypeConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Registry that maps Java types to their {@link TypeConverter} implementations.
 *
 * <p>A default registry pre-registers converters for all common primitive and
 * standard-library types. Additional converters can be registered at any time
 * via {@link #register(Class, TypeConverter)}.
 *
 * <p>Platform modules (e.g. {@code mini-config-bukkit}) extend this registry
 * by registering their own converters on top of the defaults.
 *
 * <pre>{@code
 * ConverterRegistry registry = ConverterRegistry.defaults();
 * registry.register(Duration.class, new DurationConverter());
 *
 * ConfigContainer container = new ConfigContainer(registry);
 * }</pre>
 */
public class ConverterRegistry {

    private final Map<Class<?>, TypeConverter<?>> byType = new HashMap<>();

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    /**
     * Creates a new registry pre-populated with converters for all built-in types:
     * {@code String}, {@code Integer}/{@code int}, {@code Long}/{@code long},
     * {@code Double}/{@code double}, {@code Float}/{@code float},
     * {@code Boolean}/{@code boolean}, {@link UUID}, and {@code List<String>}.
     *
     * @return a new registry with default converters registered
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static ConverterRegistry defaults() {
        ConverterRegistry r = new ConverterRegistry();

        r.register(String.class,       Converters.STRING);
        r.register(Integer.class,      Converters.INTEGER);
        r.register(int.class,          Converters.INTEGER);
        r.register(Long.class,         Converters.LONG);
        r.register(long.class,         Converters.LONG);
        r.register(Double.class,       Converters.DOUBLE);
        r.register(double.class,       Converters.DOUBLE);
        r.register(Float.class,        Converters.FLOAT);
        r.register(float.class,        Converters.FLOAT);
        r.register(Boolean.class,      Converters.BOOLEAN);
        r.register(boolean.class,      Converters.BOOLEAN);
        r.register(UUID.class,         Converters.UUID_CONVERTER);

        // List<String> erasure vs Class<List> mismatch — safe raw cast
        r.register((Class) List.class, (TypeConverter) Converters.STRING_LIST);

        return r;
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Registers a converter for the given type, replacing any previously registered one.
     *
     * @param type      the Java type this converter handles
     * @param converter the converter implementation
     * @param <T>       the type parameter
     */
    public <T> void register(Class<T> type, TypeConverter<T> converter) {
        byType.put(type, converter);
    }

    // -------------------------------------------------------------------------
    // Lookup
    // -------------------------------------------------------------------------

    /**
     * Returns the converter registered for {@code type}, or {@code null} if none.
     *
     * @param type the type to look up
     * @return the registered converter, or {@code null}
     */
    @SuppressWarnings("unchecked")
    public <T> TypeConverter<T> get(Class<T> type) {
        return (TypeConverter<T>) byType.get(type);
    }

    /**
     * Returns the converter registered for {@code type}.
     *
     * @param type the type to look up
     * @return the registered converter
     * @throws ConversionException if no converter is registered for the type
     */
    public <T> TypeConverter<T> require(Class<T> type) {
        TypeConverter<T> converter = get(type);
        if (converter == null) {
            throw new ConversionException(
                    "No converter registered for type: " + type.getName()
                    + ". Register one via ConverterRegistry#register or use @Converter on the field.");
        }
        return converter;
    }

    /**
     * Instantiates a converter class reflectively (must have a public no-arg constructor).
     *
     * @param converterClass the converter class to instantiate
     * @return a new instance of the converter
     * @throws ConversionException if instantiation fails
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeConverter<T> instantiate(Class<? extends TypeConverter<?>> converterClass) {
        try {
            return (TypeConverter<T>) converterClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ConversionException(
                    "Failed to instantiate converter: " + converterClass.getName()
                    + ". Ensure it has a public no-arg constructor.", e);
        }
    }
}
