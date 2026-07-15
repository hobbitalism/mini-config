package com.github.hobbitalism.miniconfig.yaml;

import com.github.hobbitalism.miniconfig.Config;
import com.github.hobbitalism.miniconfig.ConfigSection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * File-backed {@link Config} implementation for YAML documents.
 *
 * <p>Uses {@link YamlConfigLoader} to parse and {@link YamlConfigSerializer}
 * to write. A default resource can be supplied so the file is populated
 * on first load (similar to Bukkit's {@code saveDefaultConfig}).
 *
 * <pre>{@code
 * YamlConfig config = new YamlConfig(Path.of("plugins/MyPlugin/config.yml"));
 * config.load();
 *
 * String host = config.getString("database.host", "localhost");
 * config.set("database.host", "db.example.com");
 * config.save();
 * }</pre>
 */
public class YamlConfig implements Config {

    private final Path filePath;
    private final InputStream defaults;

    private final YamlConfigLoader   loader     = new YamlConfigLoader();
    private final YamlConfigSerializer serializer = new YamlConfigSerializer();

    /** Live data delegate — swapped out on each load. */
    private YamlConfigSection section = new YamlConfigSection();

    /**
     * Constructs a {@code YamlConfig} backed by the given file with no bundled defaults.
     *
     * @param filePath path to the YAML file (need not exist yet)
     */
    public YamlConfig(Path filePath) {
        this(filePath, null);
    }

    /**
     * Constructs a {@code YamlConfig} backed by the given file.
     * If {@code defaults} is non-null and the file does not yet exist, the
     * stream is written to disk before parsing so the file is pre-populated.
     *
     * @param filePath path to the YAML file (need not exist yet)
     * @param defaults optional default content stream (closed after first use)
     */
    public YamlConfig(Path filePath, InputStream defaults) {
        this.filePath = filePath;
        this.defaults = defaults;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void load() throws IOException {
        if (!Files.exists(filePath)) {
            Files.createDirectories(filePath.getParent() == null ? Path.of(".") : filePath.getParent());
            if (defaults != null) {
                Files.copy(defaults, filePath);
            } else {
                Files.createFile(filePath);
            }
        }
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            section = loader.load(reader);
        }
    }

    @Override
    public void save() throws IOException {
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            serializer.save(section, writer);
        }
    }

    // -------------------------------------------------------------------------
    // ConfigSection — delegate to the live section
    // -------------------------------------------------------------------------

    @Override public Set<String> getKeys(boolean deep)                        { return section.getKeys(deep); }
    @Override public boolean contains(String key)                              { return section.contains(key); }
    @Override public Optional<Object> get(String key)                         { return section.get(key); }
    @Override public String getString(String key, String defaultValue)        { return section.getString(key, defaultValue); }
    @Override public int getInt(String key, int defaultValue)                 { return section.getInt(key, defaultValue); }
    @Override public double getDouble(String key, double defaultValue)        { return section.getDouble(key, defaultValue); }
    @Override public boolean getBoolean(String key, boolean defaultValue)     { return section.getBoolean(key, defaultValue); }
    @Override public List<String> getStringList(String key)                   { return section.getStringList(key); }
    @Override public Optional<ConfigSection> getSection(String key)           { return section.getSection(key); }
    @Override public void set(String key, Object value)                       { section.set(key, value); }

    /**
     * Returns the underlying {@link YamlConfigSection} for direct access.
     *
     * @return the live config section
     */
    public YamlConfigSection getSection() {
        return section;
    }
}
