package com.github.hobbitalism.miniconfig.convert;

/**
 * Thrown when a {@link TypeConverter} cannot convert a value.
 */
public class ConversionException extends RuntimeException {

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
