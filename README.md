# MiniConfig

Annotation-driven configuration library for Java with YAML, JSON, and Bukkit support.

## Installation

**Gradle**

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    api 'com.github.hobbitalism.mini-config:mini-config-core:v0.1.1'
    api 'com.github.hobbitalism.mini-config:mini-config-yaml:v0.1.1'
    api 'com.github.hobbitalism.mini-config:mini-config-json:v0.1.1'
    api 'com.github.hobbitalism.mini-config:mini-config-bukkit:v0.1.1'
}
```

**Maven**

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.hobbitalism.mini-config</groupId>
    <artifactId>mini-config-core</artifactId>
    <version>v0.1.1</version>
</dependency>
<dependency>
    <groupId>com.github.hobbitalism.mini-config</groupId>
    <artifactId>mini-config-yaml</artifactId>
    <version>v0.1.1</version>
</dependency>
```

Replace the version with a [Git tag](https://github.com/hobbitalism/mini-config/tags) or a commit hash.

> JitPack builds require Java 21. Ensure your project targets at least JDK 21, or configure
> [jitpack.yml](jitpack.yml) if using a custom build environment.

## Modules

| Module | Artifact | Description |
|--------|----------|-------------|
| `core` | `mini-config-core` | Annotations, `ConfigSection` interface, `ConfigContainer` POJO binding, converters |
| `yaml` | `mini-config-yaml` | YAML backend via SnakeYAML |
| `json`  | `mini-config-json`  | JSON backend via Jackson |
| `bukkit` | `mini-config-bukkit` | Bukkit `FileConfiguration` adapter |

## Quick Start

### 1. Define a POJO

```java
@Config
class AppConfig {
    @Path("server.host")
    @Default("localhost")
    String host;

    @Path("server.port")
    @Default(25565)
    int port;

    @Path("server.debug")
    boolean debug;

    @Path("worlds")
    List<String> worlds;
}
```

### 2. Read from a file

```java
YamlConfig config = new YamlConfig(Path.of("config.yml"));
config.load();

AppConfig pojo = new AppConfig();
new ConfigContainer().load(pojo, config);

System.out.println(pojo.host);
```

### 3. Write back

```java
pojo.port = 8080;
new ConfigContainer().save(pojo, config);
config.save();
```

---

## Declarative File Binding

Use `@ConfigFile` to declare the backing file directly on the POJO:

```java
@ConfigFile(fileName = "config.yml", type = YamlConfig.class)
@Config
class AppConfig {
    @Path("server.host") String host;
    @Path("server.port") int port;
}
```

Then load/save with `ConfigFileManager`:

```java
ConfigFileManager mgr = new ConfigFileManager()
    .baseDir(Path.of("plugins/MyPlugin"));

// Load (providing an instance):
AppConfig cfg = mgr.load(new AppConfig());

// Load (auto-instantiates via no-arg constructor):
AppConfig cfg = mgr.load(AppConfig.class);

// Save:
mgr.save(cfg);
```

Multiple POJOs, each with their own file, share the same manager:

```java
@ConfigFile(fileName = "server.yml", type = YamlConfig.class)
@Config class ServerConfig { ... }

@ConfigFile(fileName = "database.json", type = JsonConfig.class)
@Config class DatabaseConfig { ... }

ConfigFileManager mgr = new ConfigFileManager().baseDir(path);

ServerConfig  srv = mgr.load(ServerConfig.class);
DatabaseConfig db = mgr.load(DatabaseConfig.class);
```

Override `@ConfigFile` values fluently:

```java
var cfg = new ConfigFileManager()
    .baseDir(Path.of("data"))
    .file("custom.yml")
    .format(YamlConfig.class)
    .load(AppConfig::new);
```

---

## Annotations

| Annotation | Target | Description |
|------------|--------|-------------|
| `@Config` | Type | Marks a class as a config POJO |
| `@Path` | Field | Dot-separated config path for the field |
| `@Default` | Field | Fallback value string if the key is absent |
| `@Comment` | Field | Descriptive lines written as `//` (JSON) or `#` (YAML) above the key |
| `@Ignore` | Field | Skips the field during load/save |
| `@Converter` | Field | Custom `TypeConverter` implementation for the field |
| `@PostLoad` | Method | Called after fields are loaded |
| `@PreSave` | Method | Called before fields are saved |
| `@ConfigFile` | Type | Declares the backing file and format for `ConfigFileManager` |

---

## Comments

The `@Comment` annotation adds descriptions above config keys in the output:

```java
@Config
class AppConfig {
    @Comment("The hostname to bind to")
    @Path("server.host")
    String host;

    @Comment({"The listening port", "Change this for production"})
    @Path("server.port")
    int port;
}
```

YAML output:

```yaml
# The hostname to bind to
server:
  host: localhost
# The listening port
# Change this for production
  port: 8080
```

JSON output:

```json
{
  // The hostname to bind to
  "server" : {
    "host" : "localhost",
    // The listening port
    // Change this for production
    "port" : 8080
  }
}
```

> **Note:** JSON comments (`//`) are non-standard but supported via Jackson's `ALLOW_COMMENTS` feature.

---

## Custom Converters

Register a converter for a type globally:

```java
ConverterRegistry registry = ConverterRegistry.defaults();
registry.register(Duration.class, new DurationConverter());

ConfigContainer container = new ConfigContainer(container);
```

Or annotate a specific field:

```java
@Config
class AppConfig {
    @Converter(DurationConverter.class)
    @Path("timeout")
    Duration timeout;

    @Converter(MyEnumConverter.class)
    @Path("mode")
    Mode mode;
}
```

---

## Lifecycle Hooks

```java
@Config
class AppConfig {
    @Path("host") String host;

    @PostLoad
    void onLoad() {
        System.out.println("Config loaded, host = " + host);
    }

    @PreSave
    void onSave() {
        // Validate or transform before writing
    }
}
```

---

## Bukkit Integration

```java
@ConfigFile(fileName = "config.yml", type = BukkitConfig.class)
@Config
class MyPluginConfig {
    @Path("server.host") String host;
}

public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        BukkitConfigFileManager mgr = new BukkitConfigFileManager(this);
        MyPluginConfig cfg = mgr.load(MyPluginConfig.class);

        cfg.host = "example.com";
        mgr.save(cfg);
    }
}
```

`BukkitConfigFileManager` automatically resolves `baseDir` from `plugin.getDataFolder()` and handles `BukkitConfig`'s native `saveDefaultConfig()` and default-merging lifecycle.

---

## Available Converters

| Type | Converter | Accepts |
|------|-----------|---------|
| `String` | `STRING` | `toString()` of any value |
| `int` / `Integer` | `INTEGER` | Number or parseable string |
| `long` / `Long` | `LONG` | Number or parseable string |
| `double` / `Double` | `DOUBLE` | Number or parseable string |
| `float` / `Float` | `FLOAT` | Number or parseable string |
| `boolean` / `Boolean` | `BOOLEAN` | `true`/`false`, `t`/`f`, `1`/`0` (case-insensitive) |
| `UUID` | `UUID_CONVERTER` | Standard UUID string |
| `List<String>` | `STRING_LIST` | JSON/YAML array or single string |

Enums are supported automatically by name (case-insensitive).

---

## File Formats

### YAML (`mini-config-yaml`)

```java
YamlConfig config = new YamlConfig(Path.of("config.yml"));
config.load();
config.save();
```

Supports bundled defaults (copied to disk on first access):

```java
YamlConfig config = new YamlConfig(
    Path.of("config.yml"),
    getClass().getResourceAsStream("/default-config.yml")
);
```

### JSON (`mini-config-json`)

```java
JsonConfig config = new JsonConfig(Path.of("config.json"));
config.load();
config.save();
```

### Bukkit (`mini-config-bukkit`)

```java
BukkitConfig config = new BukkitConfig(plugin, "config.yml");
config.load();
config.save();
```

Automatically calls `plugin.saveResource()` on first load and merges defaults from the plugin jar.

---

## Performance Notes

- Fields, lifecycle methods, and converters are **cached** after first access — no repeated reflection.
- Converters registered via `@Converter` are instantiated once and cached by class.
- `ConfigFileManager` uses a shared `ObjectMapper` for JSON and a per-thread `Yaml` for YAML.
- Key splitting uses a manual `indexOf` loop instead of regex.
- `ConverterRegistry` uses `ConcurrentHashMap` for thread-safe registration.
