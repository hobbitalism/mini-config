package com.github.hobbitalism.miniconfig.annotation.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a numeric field falls within the specified inclusive range
 * {@code [min, max]}.
 *
 * <p>Applicable to {@code byte}, {@code short}, {@code int}, {@code long},
 * {@code float}, {@code double}, and their boxed equivalents.
 *
 * <pre>{@code
 * @Range(min = 1, max = 100)
 * private int maxPlayers;
 *
 * @Range(min = 0.0, max = 1.0)
 * private double spawnRate;
 * }</pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Range {

    /** Inclusive lower bound (defaults to {@link Double#MIN_VALUE}, i.e. no lower bound). */
    double min() default -Double.MAX_VALUE;

    /** Inclusive upper bound (defaults to {@link Double#MAX_VALUE}, i.e. no upper bound). */
    double max() default Double.MAX_VALUE;

    /**
     * Custom message included in the exception when validation fails.
     * If empty, the framework generates a default message.
     */
    String message() default "";
}
