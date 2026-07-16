package com.github.hobbitalism.miniconfig.bukkit;

import com.github.hobbitalism.miniconfig.Config;
import com.github.hobbitalism.miniconfig.annotation.ConfigFile;
import com.github.hobbitalism.miniconfig.container.ConfigContainer;
import com.github.hobbitalism.miniconfig.container.ConfigFileManager;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.nio.file.Path;

/**
 * Bukkit-aware {@link ConfigFileManager} that constructs {@link BukkitConfig}
 * instances using a {@link Plugin} reference and loads files from the
 * plugin's data folder.
 *
 * <pre>{@code
 * @ConfigFile(fileName = "config.yml", type = BukkitConfig.class)
 * @Config
 * class MyPluginConfig {
 *     @Path("server.host") String host;
 * }
 *
 * BukkitConfigFileManager mgr = new BukkitConfigFileManager(plugin);
 * MyPluginConfig cfg = mgr.load(new MyPluginConfig());
 * // ...
 * mgr.save(cfg);
 * }</pre>
 */
public class BukkitConfigFileManager extends ConfigFileManager {

    private final Plugin plugin;

    public BukkitConfigFileManager(Plugin plugin) {
        super();
        this.plugin = plugin;
        baseDir(plugin.getDataFolder().toPath());
    }

    public BukkitConfigFileManager(Plugin plugin, ConfigContainer container) {
        super(container);
        this.plugin = plugin;
        baseDir(plugin.getDataFolder().toPath());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> Config openConfig(T target, Path baseDir) {
        ConfigFile cf = target.getClass().getAnnotation(ConfigFile.class);
        if (cf == null) {
            throw new IllegalArgumentException(
                    "Class " + target.getClass().getName()
                    + " is not annotated with @ConfigFile");
        }

        Class<? extends Config> type = cf.type();
        String fileName = cf.fileName();

        try {
            if (BukkitConfig.class.isAssignableFrom(type)) {
                Constructor<? extends Config> ctor = type.getDeclaredConstructor(
                        org.bukkit.plugin.Plugin.class, String.class);
                return ctor.newInstance(plugin, fileName);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Cannot instantiate " + type.getName() + " with (Plugin, String) constructor.", e);
        }

        return super.openConfig(target, baseDir);
    }
}
