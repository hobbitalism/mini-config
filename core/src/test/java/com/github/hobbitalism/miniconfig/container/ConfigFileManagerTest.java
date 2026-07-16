package com.github.hobbitalism.miniconfig.container;

import com.github.hobbitalism.miniconfig.annotation.Config;
import com.github.hobbitalism.miniconfig.annotation.ConfigFile;
import com.github.hobbitalism.miniconfig.annotation.Path;
import com.github.hobbitalism.miniconfig.json.JsonConfig;
import com.github.hobbitalism.miniconfig.yaml.YamlConfig;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConfigFileManagerTest {

    @ConfigFile(fileName = "test-app.yml", type = YamlConfig.class)
    @Config
    static class AppConfig {
        @Path("server.host") String host = "localhost";
        @Path("server.port") int port = 25565;
    }

    @ConfigFile(fileName = "test-app.json", type = JsonConfig.class)
    @Config
    static class JsonAppConfig {
        @Path("server.host") String host = "localhost";
        @Path("server.port") int port = 25565;
    }

    private final ConfigFileManager mgr = new ConfigFileManager();

    @Test
    void yamlRoundTrip() throws Exception {
        var tmpDir = java.nio.file.Path.of("build", "tmp");
        Files.createDirectories(tmpDir);

        AppConfig out = new AppConfig();
        out.host = "example.com";
        out.port = 8080;
        mgr.save(out, tmpDir);

        AppConfig in = mgr.load(new AppConfig(), tmpDir);
        assertEquals("example.com", in.host);
        assertEquals(8080, in.port);
    }

    @Test
    void jsonRoundTrip() throws Exception {
        var tmpDir = java.nio.file.Path.of("build", "tmp");
        Files.createDirectories(tmpDir);

        JsonAppConfig out = new JsonAppConfig();
        out.host = "example.com";
        out.port = 8080;
        mgr.save(out, tmpDir);

        JsonAppConfig in = mgr.load(new JsonAppConfig(), tmpDir);
        assertEquals("example.com", in.host);
        assertEquals(8080, in.port);
    }

    @Test
    void loadCreatesFileIfMissing() throws Exception {
        var tmpDir = java.nio.file.Path.of("build", "tmp", UUID.randomUUID().toString());
        Files.createDirectories(tmpDir);

        AppConfig cfg = mgr.load(new AppConfig(), tmpDir);
        assertNotNull(cfg);
        assertEquals("localhost", cfg.host);
        assertEquals(25565, cfg.port);
        assertTrue(Files.exists(tmpDir.resolve("test-app.yml")));
    }
}
