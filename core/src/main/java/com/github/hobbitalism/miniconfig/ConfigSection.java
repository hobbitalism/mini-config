package com.github.hobbitalism.miniconfig;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a navigable section (node) within a configuration tree.
 * A section can contain key-value pairs and nested sub-sections.
 */
public interface ConfigSection {

    /**
     * Returns the keys directly under this section.
     *
     * @param deep if {@code true}, returns all nested keys recursively
     * @return an unmodifiable set of keys
     */
    Set<String> getKeys(boolean deep);

    /**
     * Returns whether the given key exists in this section.
     */
    boolean contains(String key);

    /**
     * Returns the raw value stored at {@code key}, or {@link Optional#empty()} if absent.
     */
    Optional<Object> get(String key);

    /**
     * Returns the {@code String} value at {@code key}, or {@code defaultValue} if absent or not a string.
     */
    String getString(String key, String defaultValue);

    /**
     * Returns the {@code int} value at {@code key}, or {@code defaultValue} if absent or not an integer.
     */
    int getInt(String key, int defaultValue);

    /**
     * Returns the {@code double} value at {@code key}, or {@code defaultValue} if absent or not a double.
     */
    double getDouble(String key, double defaultValue);

    /**
     * Returns the {@code boolean} value at {@code key}, or {@code defaultValue} if absent or not a boolean.
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Returns the {@code List<String>} at {@code key}, or an empty list if absent.
     */
    List<String> getStringList(String key);

    /**
     * Returns a nested {@link ConfigSection} at {@code key}, or {@link Optional#empty()} if absent.
     */
    Optional<ConfigSection> getSection(String key);

    /**
     * Sets a value at the given key.
     */
    void set(String key, Object value);

    /**
     * Attaches a descriptive comment to a config path.
     *
     * <p>Implementations that support comments (e.g. JSON, YAML serializers)
     * may emit these as {@code //} or {@code #} lines above the corresponding key.
     * The default implementation is a no-op.
     *
     * @param path    the dot-separated config path
     * @param comment the comment text (without comment prefix)
     */
    default void setComment(String path, String comment) {
    }
}
