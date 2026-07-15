package com.github.hobbitalism.miniconfig;

import java.io.IOException;
import java.io.Writer;

/**
 * Strategy interface for serializing a {@link ConfigSection} back to text.
 *
 * @param <T> the concrete {@link ConfigSection} type accepted by this serializer
 */
public interface ConfigSerializer<T extends ConfigSection> {

    /**
     * Writes the given section to the provided {@link Writer}.
     *
     * @param section the section to serialize
     * @param writer  the writer to output to
     * @throws IOException if writing fails
     */
    void save(T section, Writer writer) throws IOException;
}
