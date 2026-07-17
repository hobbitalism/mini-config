package com.github.hobbitalism.miniconfig;

import java.io.IOException;
import java.io.InputStream;

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
      * Loads the configuration from the given input stream.
      * This populates the in-memory configuration without writing to disk.
      *
      * @param stream the input stream to read from (will not be closed)
      * @throws IOException if the stream cannot be read
      */
     void loadFromStream(InputStream stream) throws IOException;

     /**
      * Reloads the configuration, discarding any unsaved in-memory changes.
      *
      * @throws IOException if the source cannot be read
      */
     default void reload() throws IOException {
         load();
     }
}
