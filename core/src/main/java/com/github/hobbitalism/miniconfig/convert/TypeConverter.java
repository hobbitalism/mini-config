package com.github.hobbitalism.miniconfig.convert;

/**
 * Strategy interface for converting values between their raw config representation
 * (typically {@link String} or a primitive wrapper) and a richer Java type.
 *
 * <p>Implementations are referenced via {@link com.github.hobbitalism.miniconfig.annotation.Converter}
 * and must have a public no-arg constructor so they can be instantiated reflectively.
 *
 * <p>Example — {@code java.time.Duration} converter:
 * <pre>{@code
 * public class DurationConverter implements TypeConverter<Duration> {
 *
 *     @Override
 *     public Duration deserialize(Object raw) {
 *         return Duration.parse(raw.toString());
 *     }
 *
 *     @Override
 *     public Object serialize(Duration value) {
 *         return value.toString();
 *     }
 * }
 * }</pre>
 *
 * @param <T> the rich Java type produced / consumed by this converter
 */
public interface TypeConverter<T> {

    /**
     * Converts a raw config value (e.g. {@code String}, {@code Number}, {@code Map})
     * into the target type {@code T}.
     *
     * @param raw the raw value read from the config source; never {@code null}
     * @return the deserialized value
     * @throws ConversionException if the value cannot be converted
     */
    T deserialize(Object raw);

    /**
     * Converts a value of type {@code T} into a form suitable for storage
     * in the config (e.g. {@code String}, {@code Number}, {@code Map}).
     *
     * @param value the value to serialize; never {@code null}
     * @return the serializable form
     * @throws ConversionException if the value cannot be converted
     */
    Object serialize(T value);
}
