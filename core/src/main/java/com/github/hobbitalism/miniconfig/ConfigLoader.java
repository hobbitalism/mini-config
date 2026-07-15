package com.github.hobbitalism.miniconfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Strategy interface for parsing raw input into a {@link ConfigSection}.
 * Implement this to support additional formats (YAML, TOML, JSON, …).
 *
 * @param <T> the concrete {@link ConfigSection} type produced by this loader
 */
public interface ConfigLoader<T extends ConfigSection> {

    /**
     * Parses configuration from a character-based reader.
     *
     * @param reader the reader to parse
     * @return a populated {@link ConfigSection}
     * @throws IOException if reading or parsing fails
     */
    T load(Reader reader) throws IOException;

    /**
     * Parses configuration from a byte-based input stream.
     * The default implementation delegates to {@link #load(Reader)} via UTF-8.
     *
     * @param stream the stream to parse
     * @return a populated {@link ConfigSection}
     * @throws IOException if reading or parsing fails
     */
    default T load(InputStream stream) throws IOException {
        return load(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
    }
}
