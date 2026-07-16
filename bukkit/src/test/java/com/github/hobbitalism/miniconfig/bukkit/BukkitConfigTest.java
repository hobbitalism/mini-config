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


import java.io.IOException;
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

}
