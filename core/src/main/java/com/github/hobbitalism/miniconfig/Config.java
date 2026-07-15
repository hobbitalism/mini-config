package com.github.hobbitalism.miniconfig;

import java.io.IOException;

/**
 * Top-level configuration object.
 * Extends {@link ConfigSection} with load/save lifecycle methods.
 */
public interface Config extends ConfigSection {

    /**
     * Loads the configuration from its backing source (file, stream, etc.).
     *
     * @throws IOException if the source cannot be read
     */
    void load() throws IOException;

    /**
     * Saves the configuration back to its backing source.
     *
     * @throws IOException if the source cannot be written
     */
    void save() throws IOException;

    /**
     * Reloads the configuration, discarding any unsaved in-memory changes.
     *
     * @throws IOException if the source cannot be read
     */
    default void reload() throws IOException {
        load();
    }
}
