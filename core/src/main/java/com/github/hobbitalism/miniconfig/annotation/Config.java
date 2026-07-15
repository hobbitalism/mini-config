package com.github.hobbitalism.miniconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a configuration class.
 *
 * <p>The annotated class will be scanned by the config framework, which will
 * bind its fields to paths in the backing config source.
 *
 * <pre>{@code
 * @Config
 * public class PluginConfig {
 *
 *     @Path("server.name")
 *     @Default("My Server")
 *     private String serverName;
 * }
 * }</pre>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {

    /**
     * Optional root path prefix applied to all fields in this class.
     * Leave empty (default) to use no prefix.
     *
     * <p>Example: {@code @Config("plugin")} makes {@code @Path("debug")} resolve
     * to {@code plugin.debug}.
     */
    String value() default "";
}
