package com.github.hobbitalism.miniconfig.annotation.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a {@link String} field matches the given regular expression.
 *
 * <p>The pattern is matched against the entire field value via
 * {@link java.util.regex.Pattern#matches(String, CharSequence)}.
 *
 * <pre>{@code
 * @Pattern("[A-Za-z0-9_]+")
 * private String username;
 *
 * @Pattern("^#[0-9A-Fa-f]{6}$")
 * private String hexColor;
 * }</pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Pattern {

    /**
     * A valid Java regular expression. The annotated field's string value must
     * match this pattern in full.
     */
    String value();

    /**
     * {@link java.util.regex.Pattern} flags to apply (e.g.
     * {@link java.util.regex.Pattern#CASE_INSENSITIVE}). Defaults to none.
     */
    int flags() default 0;

    /**
     * Custom message included in the exception when validation fails.
     * If empty, the framework generates a default message.
     */
    String message() default "";
}
