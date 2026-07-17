package com.github.hobbitalism.miniconfig.bukkit;

import com.github.hobbitalism.miniconfig.annotation.Config;
import com.github.hobbitalism.miniconfig.annotation.ConfigFile;
import com.github.hobbitalism.miniconfig.annotation.Default;
import com.github.hobbitalism.miniconfig.annotation.Path;
import com.github.hobbitalism.miniconfig.container.ConfigContainer;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BukkitConfigTest {

    ServerMock server;
    Plugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @ConfigFile(fileName = "test-config.yml", type = BukkitConfig.class)
    @Config
    static class TestConfig {
        @Default("localhost")
        @Path("server.host")
        private String host;

        @Default("25565")
        @Path("server.port")
        private int port;

        @Default("false")
        @Path("server.debug")
        private boolean debug;
    }

    // -------------------------------------------------------------------------
    // BukkitConfig direct tests
    // -------------------------------------------------------------------------

    @Test
    void bukkitConfigRoundTrip() throws IOException {
        var dataFolder = plugin.getDataFolder().toPath();
        Files.createDirectories(dataFolder);

        BukkitConfig config = new BukkitConfig(plugin, "roundtrip.yml");
        config.load();

        config.set("name", "test");
        config.set("count", 42);
        config.set("active", true);
        config.save();

        BukkitConfig reloaded = new BukkitConfig(plugin, "roundtrip.yml");
        reloaded.load();

        assertEquals("test", reloaded.getString("name", ""));
        assertEquals(42, reloaded.getInt("count", 0));
        assertTrue(reloaded.getBoolean("active", false));
    }

    @Test
    void bukkitConfig_saveResourceOnFirstLoad() throws IOException {
        var configFile = plugin.getDataFolder().toPath().resolve("new-config.yml");
        assertFalse(Files.exists(configFile));

        BukkitConfig config = new BukkitConfig(plugin, "new-config.yml");
        config.load();

        assertTrue(Files.exists(configFile));
    }

    // -------------------------------------------------------------------------
    // BukkitConfigFileManager tests
    // -------------------------------------------------------------------------

    @Test
    void fileManager_defaultsWhenFileMissing() throws IOException {
        BukkitConfigFileManager mgr = new BukkitConfigFileManager(plugin);

        TestConfig cfg = mgr.load(TestConfig.class);
        assertEquals("localhost", cfg.host);
        assertEquals(25565, cfg.port);
        assertFalse(cfg.debug);
    }

    @Test
    void fileManagerRoundTrip() throws IOException {
        BukkitConfigFileManager mgr = new BukkitConfigFileManager(plugin);

        TestConfig out = new TestConfig();
        out.host = "example.com";
        out.port = 8080;
        out.debug = true;
        mgr.save(out);

        TestConfig in = mgr.load(TestConfig.class);
        assertEquals("example.com", in.host);
        assertEquals(8080, in.port);
        assertTrue(in.debug);
    }

    @Test
    void fileManager_reloadPreservesFields() throws IOException {
        BukkitConfigFileManager mgr = new BukkitConfigFileManager(plugin);

        TestConfig cfg = new TestConfig();
        cfg.host = "example.com";
        cfg.port = 8080;
        mgr.save(cfg);

        // Mutate the in-memory object
        cfg.host = "other.com";
        cfg.port = 9090;

        // Reload from disk — should get saved values back
        mgr.reload(cfg);
        assertEquals("example.com", cfg.host);
        assertEquals(8080, cfg.port);
    }

    // -------------------------------------------------------------------------
    // POJO binding with ConfigContainer + BukkitConfigSection
    // -------------------------------------------------------------------------

    @Config
    static class ServerConfig {
        @Path("server.host")
        @Default("localhost")
        String host;

        @Path("server.port")
        @Default("25565")
        int port;

        @Path("server.debug")
        boolean debug;
    }

    @Test
    void pojoBindingWithBukkitConfig() throws IOException {
        BukkitConfig config = new BukkitConfig(plugin, "binding-test.yml");
        config.load();

        ServerConfig pojo = new ServerConfig();
        pojo.host = "example.com";
        pojo.port = 8080;
        pojo.debug = true;

        ConfigContainer container = new ConfigContainer();
        container.save(pojo, config);
        config.save();

        ServerConfig result = new ServerConfig();
        container.load(result, config);

        assertEquals("example.com", result.host);
        assertEquals(8080, result.port);
        assertTrue(result.debug);
    }

    // -------------------------------------------------------------------------
    // loadFromStream tests
    // -------------------------------------------------------------------------

    @Test
    void loadFromStream_loadsYamlData() throws IOException {
        String yaml = """
                server:
                  host: stream.example.com
                  port: 9090
                  debug: true
                worlds:
                  - survival
                  - creative
                """;
        InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

        BukkitConfig config = new BukkitConfig(plugin, "stream-test.yml");
        config.loadFromStream(stream);

        assertEquals("stream.example.com", config.getString("server.host", ""));
        assertEquals(9090, config.getInt("server.port", 0));
        assertTrue(config.getBoolean("server.debug", false));
        assertEquals(List.of("survival", "creative"), config.getStringList("worlds"));
    }

    @Test
    void loadFromStream_pojoBinding() throws IOException {
        String yaml = """
                server:
                  host: pojo.example.com
                  port: 5555
                  debug: true
                """;
        InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

        BukkitConfig config = new BukkitConfig(plugin, "pojo-test.yml");
        config.loadFromStream(stream);

        ServerConfig pojo = new ServerConfig();
        ConfigContainer container = new ConfigContainer();
        container.load(pojo, config);

        assertEquals("pojo.example.com", pojo.host);
        assertEquals(5555, pojo.port);
        assertTrue(pojo.debug);
    }

    @Test
    void loadFromStream_withComments() throws IOException {
        String yaml = """
                # This is a comment
                server:
                  host: commented.example.com
                  port: 7777 # inline comment
                  debug: false
                """;
        InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

        BukkitConfig config = new BukkitConfig(plugin, "comment-test.yml");
        config.loadFromStream(stream);

        assertEquals("commented.example.com", config.getString("server.host", ""));
        assertEquals(7777, config.getInt("server.port", 0));
        assertFalse(config.getBoolean("server.debug", true));
    }

    @Test
    void loadFromStream_multipleLoads() throws IOException {
        String yaml1 = """
                server:
                  host: first.example.com
                  port: 1111
                """;
        String yaml2 = """
                server:
                  host: second.example.com
                  port: 2222
                """;

        BukkitConfig config = new BukkitConfig(plugin, "multi-test.yml");

        // Load first stream
        config.loadFromStream(new ByteArrayInputStream(yaml1.getBytes(StandardCharsets.UTF_8)));
        assertEquals("first.example.com", config.getString("server.host", ""));
        assertEquals(1111, config.getInt("server.port", 0));

        // Load second stream — should replace
        config.loadFromStream(new ByteArrayInputStream(yaml2.getBytes(StandardCharsets.UTF_8)));
        assertEquals("second.example.com", config.getString("server.host", ""));
        assertEquals(2222, config.getInt("server.port", 0));
    }

    @Test
    void loadFromStream_doesNotWriteToFile() throws IOException {
        String yaml = """
                server:
                  host: nofile.example.com
                  port: 3333
                """;
        InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

        BukkitConfig config = new BukkitConfig(plugin, "nofile-test.yml");
        config.loadFromStream(stream);

        // Data is loaded in memory
        assertEquals("nofile.example.com", config.getString("server.host", ""));

        // But file should not exist yet
        var configFile = plugin.getDataFolder().toPath().resolve("nofile-test.yml");
        assertFalse(Files.exists(configFile));

        // Only after save should it exist
        config.save();
        assertTrue(Files.exists(configFile));
    }

    @Test
    void loadFromStream_withDefaults() throws IOException {
        String yaml = """
                server:
                  host: defaults.example.com
                """;
        InputStream stream = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

        BukkitConfig config = new BukkitConfig(plugin, "defaults-test.yml");
        config.loadFromStream(stream);

        // Value from stream
        assertEquals("defaults.example.com", config.getString("server.host", ""));

        // Value not in stream should use default value
        assertEquals(25565, config.getInt("server.port", 25565));
    }

}
