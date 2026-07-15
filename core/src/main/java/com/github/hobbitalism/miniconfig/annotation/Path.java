package com.github.hobbitalism.miniconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a field or nested config object to an explicit dot-separated path in
 * the configuration source.
 *
 * <p>When omitted, the framework falls back to the field name as the path
 * segment (useful for nested objects where the field name matches the YAML key).
 *
 * <pre>{@code
 * @Path("database.host")
 * private String host;
 *
 * @Path("messages.prefix")
 * private String prefix;
 * }</pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

    /**
     * Dot-separated config path (e.g. {@code "database.host"}).
     */
    String value();
}
