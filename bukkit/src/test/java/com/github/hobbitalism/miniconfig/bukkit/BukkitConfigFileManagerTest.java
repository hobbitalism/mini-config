package com.github.hobbitalism.miniconfig.bukkit;

import com.github.hobbitalism.miniconfig.annotation.Config;
import com.github.hobbitalism.miniconfig.annotation.ConfigFile;
import com.github.hobbitalism.miniconfig.annotation.Default;
import com.github.hobbitalism.miniconfig.annotation.Path;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class BukkitConfigFileManagerTest {

    private ServerMock server;
    private Plugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @ConfigFile(fileName = "app.yml", type = BukkitConfig.class)
    @Config
    static class AppConfig {
        @Default("localhost")
        @Path("host")  String host;
        @Default("8080")
        @Path("port")  int port;
    }

    @Test
    void load_populatesDefaultsWhenFileMissing() throws IOException {
        BukkitConfigFileManager mgr = new BukkitConfigFileManager(plugin);

        AppConfig cfg = mgr.load(AppConfig.class);
        assertEquals("localhost", cfg.host);
        assertEquals(8080, cfg.port);
    }

    @Test
    void saveAndLoad_roundTrip() throws IOException {
        BukkitConfigFileManager mgr = new BukkitConfigFileManager(plugin);

        AppConfig out = new AppConfig();
        out.host = "example.com";
        out.port = 9090;
        mgr.save(out);

        AppConfig in = mgr.load(AppConfig.class);
        assertEquals("example.com", in.host);
        assertEquals(9090, in.port);
    }

    @Test
    void reload_overwritesInMemoryChanges() throws IOException {
        BukkitConfigFileManager mgr = new BukkitConfigFileManager(plugin);

        AppConfig cfg = new AppConfig();
        cfg.host = "saved.example.com";
        cfg.port = 7070;
        mgr.save(cfg);

        cfg.host = "modified.example.com";
        cfg.port = 6060;

        mgr.reload(cfg);
        assertEquals("saved.example.com", cfg.host);
        assertEquals(7070, cfg.port);
    }

    @Test
    void load_existingInstance() throws IOException {
        BukkitConfigFileManager mgr = new BukkitConfigFileManager(plugin);

        AppConfig out = new AppConfig();
        out.host = "persistent.example.com";
        out.port = 5050;
        mgr.save(out);

        AppConfig existing = new AppConfig();
        existing.host = "should-be-overwritten";
        existing.port = 4040;

        mgr.load(existing);
        assertEquals("persistent.example.com", existing.host);
        assertEquals(5050, existing.port);
    }

    @Test
    void openConfig_usesBukkitConfigConstructor() {
        BukkitConfigFileManager mgr = new BukkitConfigFileManager(plugin);
        AppConfig cfg = new AppConfig();

        com.github.hobbitalism.miniconfig.Config config = mgr.openConfig(cfg, plugin.getDataFolder().toPath());
        assertInstanceOf(BukkitConfig.class, config);
    }

    @Test
    void constructor_setsBaseDir() throws IOException {
        BukkitConfigFileManager mgr = new BukkitConfigFileManager(plugin);

        AppConfig cfg = mgr.load(AppConfig.class);
        assertNotNull(cfg);
    }

    @Test
    void multipleFiles_withDifferentPOJOs() throws IOException {
        @ConfigFile(fileName = "alpha.yml", type = BukkitConfig.class)
        @Config
        class Alpha {
            @Path("value")  String value;
        }

        @ConfigFile(fileName = "beta.yml", type = BukkitConfig.class)
        @Config
        class Beta {
            @Path("value")  String value;
        }

        BukkitConfigFileManager mgr = new BukkitConfigFileManager(plugin);

        Alpha a = new Alpha(); a.value = "alpha-value";
        Beta  b = new Beta();  b.value = "beta-value";
        mgr.save(a);
        mgr.save(b);

        assertEquals("alpha-value", mgr.load(new Alpha()).value);
        assertEquals("beta-value",  mgr.load(new Beta()).value);
    }
}
