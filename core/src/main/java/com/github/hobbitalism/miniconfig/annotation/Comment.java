package com.github.hobbitalism.miniconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Attaches one or more comment lines to a config field or nested section.
 *
 * <p>Comments are written above the corresponding key when the config is saved.
 * Each element in {@link #value()} becomes a separate {@code # ...} line.
 *
 * <pre>{@code
 * @Comment({
 *     "Database connection settings",
 *     "Do not change while the server is running"
 * })
 * @Path("database.host")
 * private String host;
 * }</pre>
 *
 * Produces:
 * <pre>
 * # Database connection settings
 * # Do not change while the server is running
 * database:
 *   host: localhost
 * </pre>
 *
 * <p>This annotation may also be placed on a type annotated with {@link Config}
 * to emit a header comment at the top of the file.
 */
@Documented
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Comment {

    /**
     * One or more comment lines. Each entry is written as a separate {@code #} line.
     * Empty strings produce blank comment lines ({@code #}).
     */
    String[] value();
}
