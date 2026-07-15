package com.github.hobbitalism.miniconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be called immediately after the config has been loaded
 * and all fields have been populated.
 *
 * <p>Use this for cross-field validation, derived-value computation, or any
 * other post-load setup that requires all fields to already be set.
 *
 * <p>Requirements:
 * <ul>
 *   <li>The method must be {@code public} or package-private.</li>
 *   <li>The method must take no parameters.</li>
 *   <li>The return value is ignored.</li>
 *   <li>If the method throws a {@link RuntimeException} the load is aborted.</li>
 * </ul>
 *
 * <pre>{@code
 * @PostLoad
 * public void validate() {
 *     if (maxPlayers < 1)
 *         throw new IllegalStateException("maxPlayers must be >= 1");
 * }
 * }</pre>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostLoad {
}
