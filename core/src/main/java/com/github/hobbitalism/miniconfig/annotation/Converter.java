package com.github.hobbitalism.miniconfig.annotation;

import com.github.hobbitalism.miniconfig.convert.TypeConverter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Designates a custom {@link TypeConverter} for a field whose type cannot be
 * handled by the default conversion rules.
 *
 * <p>The referenced converter class must have a public no-arg constructor and
 * will be instantiated once per binding.
 *
 * <pre>{@code
 * @Converter(DurationConverter.class)
 * private Duration timeout;
 *
 * @Converter(LocationConverter.class)
 * private Location spawnPoint;
 * }</pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Converter {

    /**
     * The {@link TypeConverter} implementation to use for this field.
     */
    Class<? extends TypeConverter<?>> value();
}
