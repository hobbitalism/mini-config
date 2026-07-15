package com.github.hobbitalism.miniconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes a field from config serialization and deserialization entirely.
 *
 * <p>Use this for fields that are runtime-only (loggers, caches, injected
 * services, etc.) and should never appear in the config file.
 *
 * <pre>{@code
 * @Ignore
 * private transient Logger logger;
 *
 * @Ignore
 * private transient CachedValue cache;
 * }</pre>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignore {
}
