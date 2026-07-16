package com.github.hobbitalism.miniconfig.annotation;

import com.github.hobbitalism.miniconfig.Config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the backing config file for a {@link Config @Config}-annotated POJO.
 *
 * <pre>{@code
 * @ConfigFile(fileName = "database.yml", type = YamlConfig.class)
 * @Config
 * class DatabaseConfig {
 *     @Path("host")
 *     String host = "localhost";
 * }
 * }</pre>
 *
 * @see com.github.hobbitalism.miniconfig.container.ConfigFileManager
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigFile {

    /** File name, relative to a base directory supplied at runtime. */
    String fileName();

    /** The {@link Config} implementation class to use (e.g. {@code YamlConfig.class}). */
    Class<? extends Config> type();
}
