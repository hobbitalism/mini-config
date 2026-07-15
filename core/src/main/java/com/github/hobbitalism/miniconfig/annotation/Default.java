package com.github.hobbitalism.miniconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a default value for a config field when the path is absent from
 * the backing config source.
 *
 * <p>The value is always expressed as a {@link String} and will be coerced
 * to the field's declared type by the framework (or by a {@link Converter}
 * if one is present).
 *
 * <pre>{@code
 * @Default("localhost")
 * @Path("database.host")
 * private String host;
 *
 * @Default("3306")
 * private int port;
 * }</pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Default {

    /**
     * The default value as a string. The framework coerces it to the field type.
     */
    String value();
}
