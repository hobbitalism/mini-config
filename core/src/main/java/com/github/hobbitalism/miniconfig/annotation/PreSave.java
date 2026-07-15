package com.github.hobbitalism.miniconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be called immediately before the config is serialized
 * and written back to its backing source.
 *
 * <p>Use this to normalize, sanitize, or derive values before they are
 * persisted.
 *
 * <p>Requirements:
 * <ul>
 *   <li>The method must be {@code public} or package-private.</li>
 *   <li>The method must take no parameters.</li>
 *   <li>The return value is ignored.</li>
 *   <li>If the method throws a {@link RuntimeException} the save is aborted.</li>
 * </ul>
 *
 * <pre>{@code
 * @PreSave
 * public void normalize() {
 *     serverName = serverName.trim();
 * }
 * }</pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PreSave {
}
