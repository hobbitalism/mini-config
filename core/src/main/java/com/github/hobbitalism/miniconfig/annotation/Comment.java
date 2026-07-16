package com.github.hobbitalism.miniconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Attaches one or more description lines to a config field.
 *
 * <p>Descriptions are written above the corresponding key when the config is saved.
 * Each serializer emits them using its native comment syntax (e.g. {@code //} for JSON,
 * {@code #} for YAML).
 *
 * <pre>{@code
 * @Comment({
 *     "Database connection settings",
 *     "Do not change while the server is running"
 * })
 * @Path("database.host")
 * private String host;
 * }</pre>
 */
@Documented
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Comment {

    /**
     * One or more description lines.
     * Each element is emitted as a separate commented line using the
     * target format's native comment prefix.
     */
    String[] value();
}
