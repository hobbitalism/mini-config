package com.github.hobbitalism.miniconfig.bukkit;

import com.github.hobbitalism.miniconfig.Config;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@link Config} implementation backed by Bukkit's {@link FileConfiguration} (YAML).
 *
 * <p>Usage:
 * <pre>{@code
 * BukkitConfig cfg = new BukkitConfig(plugin, "config.yml");
 * cfg.load();
 * String value = cfg.getString("some.key", "default");
 * }</pre>
 */
public class BukkitConfig implements Config {

    private final Plugin plugin;
    private final String fileName;
    private final File configFile;

    private FileConfiguration handle;

    public BukkitConfig(Plugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.configFile = new File(plugin.getDataFolder(), fileName);
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void load() throws IOException {
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            if (plugin.getResource(fileName) != null) {
                plugin.saveResource(fileName, false);
            } else {
                configFile.createNewFile();
            }
        }
        handle = YamlConfiguration.loadConfiguration(configFile);

        try (InputStream defaultStream = plugin.getResource(fileName)) {
            if (defaultStream != null) {
                try (BufferedReader reader = new BufferedReader(
                        new java.io.InputStreamReader(defaultStream, StandardCharsets.UTF_8))) {
                    YamlConfiguration defaults = YamlConfiguration.loadConfiguration(reader);
                    handle.setDefaults(defaults);
                }
            }
        }
    }

    @Override
    public void save() throws IOException {
        try {
            handle.save(configFile);
        } catch (Exception e) {
            throw new IOException("Failed to save config: " + configFile.getPath(), e);
        }
    }

    // -------------------------------------------------------------------------
    // ConfigSection — read
    // -------------------------------------------------------------------------

    @Override
    public Set<String> getKeys(boolean deep) {
        return handle.getKeys(deep);
    }

    @Override
    public boolean contains(String key) {
        return handle.contains(key);
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(handle.get(key));
    }

    @Override
    public String getString(String key, String defaultValue) {
        return handle.getString(key, defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return handle.getInt(key, defaultValue);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return handle.getDouble(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return handle.getBoolean(key, defaultValue);
    }

    @Override
    public List<String> getStringList(String key) {
        return handle.getStringList(key);
    }

    @Override
    public Optional<com.github.hobbitalism.miniconfig.ConfigSection> getSection(String key) {
        org.bukkit.configuration.ConfigurationSection section = handle.getConfigurationSection(key);
        if (section == null) return Optional.empty();
        return Optional.of(new BukkitConfigSection(section));
    }

    // -------------------------------------------------------------------------
    // ConfigSection — write
    // -------------------------------------------------------------------------

    @Override
    public void set(String key, Object value) {
        handle.set(key, value);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Provides direct access to the underlying Bukkit {@link FileConfiguration}.
     * Prefer the {@link Config} API where possible.
     */
    public FileConfiguration getHandle() {
        return handle;
    }
}
