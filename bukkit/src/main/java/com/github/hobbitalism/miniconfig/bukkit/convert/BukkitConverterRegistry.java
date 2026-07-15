package com.github.hobbitalism.miniconfig.bukkit.convert;

import com.github.hobbitalism.miniconfig.container.ConverterRegistry;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.UUID;

/**
 * A {@link ConverterRegistry} pre-populated with all built-in converters
 * <em>plus</em> the three Bukkit-specific converters.
 *
 * <p>Use this instead of {@link ConverterRegistry#defaults()} when building a
 * {@link com.github.hobbitalism.miniconfig.container.ConfigContainer} inside a
 * Bukkit plugin:
 *
 * <pre>{@code
 * ConverterRegistry registry = BukkitConverterRegistry.create();
 * ConfigContainer container = new ConfigContainer(registry);
 * }</pre>
 *
 * <p>Registered Bukkit converters:
 * <ul>
 *   <li>{@link Material}   → {@link MaterialConverter}</li>
 *   <li>{@link Sound}      → {@link SoundConverter}</li>
 *   <li>{@link UUID} (player identity) — already in defaults, but the
 *       {@link OfflinePlayerConverter} is also registered for documentation
 *       clarity when used with
 *       {@link com.github.hobbitalism.miniconfig.bukkit.annotation.OfflinePlayer @OfflinePlayer}</li>
 * </ul>
 */
public final class BukkitConverterRegistry {

    private BukkitConverterRegistry() {}

    /**
     * Creates a new registry with all default converters plus the Bukkit-specific ones.
     *
     * @return a ready-to-use registry for Bukkit plugins
     */
    public static ConverterRegistry create() {
        ConverterRegistry registry = ConverterRegistry.defaults();
        registry.register(Material.class, new MaterialConverter());
        registry.register(Sound.class,    new SoundConverter());
        // UUID is already registered by defaults(); OfflinePlayerConverter is
        // functionally identical but kept here so plugins can swap it if needed.
        registry.register(UUID.class,     new OfflinePlayerConverter());
        return registry;
    }
}
