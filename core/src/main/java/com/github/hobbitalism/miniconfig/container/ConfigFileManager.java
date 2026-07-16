package com.github.hobbitalism.miniconfig.container;

import com.github.hobbitalism.miniconfig.Config;
import com.github.hobbitalism.miniconfig.annotation.ConfigFile;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Path;

/**
 * Binds a {@link Config @Config}-annotated POJO to a file-backed {@link Config}
 * via the {@link ConfigFile @ConfigFile} annotation.
 *
 * <pre>{@code
 * @ConfigFile(fileName = "config.yml", type = YamlConfig.class)
 * @Config
 * class AppConfig {
 *     @Path("host") String host;
 *     @Path("port") int port;
 * }
 *
 * // Fluent usage — reads @ConfigFile from the POJO class:
 * AppConfig cfg = new ConfigFileManager()
 *     .baseDir(Path.of("plugins/MyPlugin"))
 *     .load(new AppConfig());
 *
 * // Explicit overrides (ignores @ConfigFile):
 * var cfg = new ConfigFileManager()
 *     .baseDir(Path.of("plugins"))
 *     .file("custom.yml")
 *     .format(YamlConfig.class)
 *     .load(new AppConfig());
 * }</pre>
 */
public class ConfigFileManager {

    private final ConfigContainer container;
    private Path baseDir;
    private String fileName;
    private Class<? extends Config> format;

    public ConfigFileManager() {
        this(new ConfigContainer());
    }

    public ConfigFileManager(ConfigContainer container) {
        this.container = container;
    }

    // -------------------------------------------------------------------------
    // Fluent setters
    // -------------------------------------------------------------------------

    /** Base directory for the config file. */
    public ConfigFileManager baseDir(Path baseDir) {
        this.baseDir = baseDir;
        return this;
    }

    /** Override the file name declared in {@code @ConfigFile}. */
    public ConfigFileManager file(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /** Override the config format declared in {@code @ConfigFile}. */
    public ConfigFileManager format(Class<? extends Config> format) {
        this.format = format;
        return this;
    }

    // -------------------------------------------------------------------------
    // Terminal operations (use stored baseDir)
    // -------------------------------------------------------------------------

    /**
     * Loads config from the declared file into a new instance of {@code type}.
     * The type must have a public no-arg constructor (as most {@code @Config} POJOs do).
     *
     * @throws IllegalStateException if no {@link #baseDir} was set
     */
    public <T> T load(Class<T> type) throws IOException {
        try {
            Constructor<T> ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            return load(ctor.newInstance());
        } catch (ReflectiveOperationException e) {
            throw new ConfigBindingException("Cannot instantiate " + type.getName()
                    + ". Ensure it has a public no-arg constructor.", e);
        }
    }

    /**
     * Loads config from the declared file into {@code target}.
     *
     * @throws IllegalStateException if no {@link #baseDir} was set
     */
    public <T> T load(T target) throws IOException {
        requireBaseDir();
        return load(target, baseDir);
    }

    /**
     * Saves config from {@code target} to the declared file.
     *
     * @throws IllegalStateException if no {@link #baseDir} was set
     */
    public <T> void save(T target) throws IOException {
        requireBaseDir();
        save(target, baseDir);
    }

    /**
     * Reloads config into {@code target} in-place.
     *
     * @throws IllegalStateException if no {@link #baseDir} was set
     */
    public <T> T reload(T target) throws IOException {
        return load(target);
    }

    // -------------------------------------------------------------------------
    // Terminal operations with explicit baseDir
    // -------------------------------------------------------------------------

    public <T> T load(T target, Path baseDir) throws IOException {
        Config config = openConfig(target, baseDir);
        config.load();
        container.load(target, config);
        return target;
    }

    public <T> void save(T target, Path baseDir) throws IOException {
        Config config = openConfig(target, baseDir);
        container.save(target, config);
        config.save();
    }

    public <T> T reload(T target, Path baseDir) throws IOException {
        return load(target, baseDir);
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private void requireBaseDir() {
        if (baseDir == null) {
            throw new IllegalStateException(
                    "Call .baseDir(path) before load/save, or use load(target, path).");
        }
    }

    protected <T> Config openConfig(T target, Path baseDir) {
        String resolvedFile = this.fileName;
        Class<? extends Config> resolvedType = this.format;

        if (resolvedFile == null || resolvedType == null) {
            ConfigFile cf = target.getClass().getAnnotation(ConfigFile.class);
            if (cf == null) {
                throw new IllegalArgumentException(
                        "Class " + target.getClass().getName()
                        + " is not annotated with @ConfigFile. "
                        + "Either add @ConfigFile or call .file()/.format().");
            }
            if (resolvedFile == null) resolvedFile = cf.fileName();
            if (resolvedType == null) resolvedType = cf.type();
        }

        Path filePath = baseDir.resolve(resolvedFile);

        try {
            try {
                Constructor<? extends Config> ctor = resolvedType.getDeclaredConstructor(Path.class);
                return ctor.newInstance(filePath);
            } catch (NoSuchMethodException e) {
                Constructor<? extends Config> ctor = resolvedType.getDeclaredConstructor(String.class);
                return ctor.newInstance(filePath.toString());
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(
                    "Cannot instantiate " + resolvedType.getName()
                    + " with a Path or String constructor. "
                    + "Ensure the module is on the classpath.", e);
        }
    }
}
