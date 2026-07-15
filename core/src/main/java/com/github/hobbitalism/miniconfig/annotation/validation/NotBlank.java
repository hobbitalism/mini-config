package com.github.hobbitalism.miniconfig.annotation.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a {@link String} field is neither {@code null} nor blank
 * (i.e. contains at least one non-whitespace character).
 *
 * <p>This is equivalent to {@code !value.isBlank()}.
 *
 * <pre>{@code
 * @NotBlank
 * private String serverName;
 * }</pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBlank {

    /**
     * Custom message included in the exception when validation fails.
     * If empty, the framework generates a default message.
     */
    String message() default "";
}
