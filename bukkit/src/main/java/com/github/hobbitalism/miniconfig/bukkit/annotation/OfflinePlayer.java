package com.github.hobbitalism.miniconfig.bukkit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signals that a {@link java.util.UUID} field represents an offline player
 * identity and should be stored / loaded as a UUID string.
 *
 * <p>This annotation plugs into the converter system: it is shorthand for
 * supplying an {@code OfflinePlayerConverter} via
 * {@link com.github.hobbitalism.miniconfig.annotation.Converter @Converter}.
 * Implementations may also look up the player name for display purposes.
 *
 * <pre>{@code
 * @OfflinePlayer
 * private java.util.UUID owner;
 * }</pre>
 *
 * Config:
 * <pre>
 * owner: "550e8400-e29b-41d4-a716-446655440000"
 * </pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OfflinePlayer {
}
