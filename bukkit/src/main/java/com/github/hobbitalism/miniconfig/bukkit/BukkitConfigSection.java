package com.github.hobbitalism.miniconfig.bukkit;

import com.github.hobbitalism.miniconfig.ConfigSection;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@link ConfigSection} adapter wrapping a Bukkit {@link ConfigurationSection}.
 */
public class BukkitConfigSection implements ConfigSection {

    private final ConfigurationSection handle;

    public BukkitConfigSection(ConfigurationSection handle) {
        this.handle = handle;
    }

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
    public Optional<ConfigSection> getSection(String key) {
        ConfigurationSection sub = handle.getConfigurationSection(key);
        if (sub == null) return Optional.empty();
        return Optional.of(new BukkitConfigSection(sub));
    }

    @Override
    public void set(String key, Object value) {
        handle.set(key, value);
    }

    /** Direct access to the underlying Bukkit section. */
    public ConfigurationSection getHandle() {
        return handle;
    }
}
