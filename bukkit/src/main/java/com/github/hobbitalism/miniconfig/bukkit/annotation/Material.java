package com.github.hobbitalism.miniconfig.bukkit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signals that a {@link org.bukkit.Material} field should be deserialized from
 * a config string by name (case-insensitive) using
 * {@link org.bukkit.Material#matchMaterial(String)}.
 *
 * <p>This annotation plugs into the converter system: it is shorthand for
 * supplying a {@code MaterialConverter} via
 * {@link com.github.hobbitalism.miniconfig.annotation.Converter @Converter}.
 *
 * <pre>{@code
 * @Material
 * private org.bukkit.Material icon = org.bukkit.Material.DIAMOND;
 * }</pre>
 *
 * Config:
 * <pre>
 * icon: DIAMOND
 * </pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Material {
}
