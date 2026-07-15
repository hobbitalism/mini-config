package com.github.hobbitalism.miniconfig.container;

/**
 * Thrown when {@link ConfigContainer} cannot bind a field to or from a config section.
 *
 * <p>This is an unchecked exception so callers do not need to declare it,
 * consistent with how most modern frameworks handle binding failures.
 */
public class ConfigBindingException extends RuntimeException {

    /**
     * @param message description of the binding failure
     */
    public ConfigBindingException(String message) {
        super(message);
    }

    /**
     * @param message description of the binding failure
     * @param cause   the underlying exception
     */
    public ConfigBindingException(String message, Throwable cause) {
        super(message, cause);
    }
}
