package com.github.hobbitalism.miniconfig.bukkit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Signals that an {@link org.bukkit.Sound} field should be deserialized from
 * a config string by name (case-insensitive) using
 * {@link org.bukkit.Sound#valueOf(String)}.
 *
 * <p>This annotation plugs into the converter system: it is shorthand for
 * supplying a {@code SoundConverter} via
 * {@link com.github.hobbitalism.miniconfig.annotation.Converter @Converter}.
 *
 * <pre>{@code
 * @Sound
 * private org.bukkit.Sound clickSound = org.bukkit.Sound.UI_BUTTON_CLICK;
 * }</pre>
 *
 * Config:
 * <pre>
 * clickSound: UI_BUTTON_CLICK
 * </pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sound {
}
