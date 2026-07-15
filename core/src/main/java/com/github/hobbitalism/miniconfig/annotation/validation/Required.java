package com.github.hobbitalism.miniconfig.annotation.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fails config loading if the annotated field is missing from the config source
 * or resolves to {@code null}.
 *
 * <p>Unlike {@link com.github.hobbitalism.miniconfig.annotation.Default}, which
 * supplies a fallback, {@code @Required} signals that the value <em>must</em>
 * be provided explicitly by the user.
 *
 * <pre>{@code
 * @Required
 * private String apiToken;
 * }</pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Required {

    /**
     * Custom message included in the exception when validation fails.
     * If empty, the framework generates a default message.
     */
    String message() default "";
}
