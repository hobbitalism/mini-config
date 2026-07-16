package com.github.hobbitalism.miniconfig.yaml;

import com.github.hobbitalism.miniconfig.annotation.Comment;
import com.github.hobbitalism.miniconfig.annotation.Config;
import com.github.hobbitalism.miniconfig.annotation.Default;
import com.github.hobbitalism.miniconfig.annotation.Path;
import com.github.hobbitalism.miniconfig.container.ConfigContainer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class YamlConfigTest {

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

        @Path("worlds")
        List<String> worlds;
    }

    @Config
    static class CommentedConfig {
        @Comment("The hostname to bind to")
        @Path("server.host")
        String host;

        @Comment({"The listening port", "Change this to 443 for production"})
        @Path("server.port")
        int port;

        @Path("server.debug")
        boolean debug;
    }

    @Test
    void pojoRoundTrip() throws IOException {
        java.nio.file.Path tmpDir = java.nio.file.Path.of("build", "tmp");
        Files.createDirectories(tmpDir);

        UUID uuid = UUID.randomUUID();
        java.nio.file.Path configFile = tmpDir.resolve(uuid + ".yml");
        System.out.println("Temp file: " + configFile.toAbsolutePath());

        YamlConfig config = new YamlConfig(configFile);
        config.load();

        ServerConfig pojo = new ServerConfig();
        pojo.host = "example.com";
        pojo.port = 8080;
        pojo.debug = true;
        pojo.worlds = List.of("nether", "end");

        ConfigContainer container = new ConfigContainer();
        container.save(pojo, config);
        config.save();

        YamlConfig reloaded = new YamlConfig(configFile);
        reloaded.load();

        ServerConfig result = new ServerConfig();
        container.load(result, reloaded);

        assertEquals("example.com", result.host);
        assertEquals(8080, result.port);
        assertTrue(result.debug);
        assertEquals(List.of("nether", "end"), result.worlds);
    }

    @Test
    void pojoWithComments_writesCommentLines() throws IOException {
        java.nio.file.Path tmpDir = java.nio.file.Path.of("build", "tmp");
        Files.createDirectories(tmpDir);

        java.nio.file.Path configFile = tmpDir.resolve(UUID.randomUUID() + ".yml");
        System.out.println("Temp file: " + configFile.toAbsolutePath());

        YamlConfig config = new YamlConfig(configFile);
        config.load();

        CommentedConfig pojo = new CommentedConfig();
        pojo.host = "example.com";
        pojo.port = 8080;
        pojo.debug = true;

        ConfigContainer container = new ConfigContainer();
        container.save(pojo, config);
        config.save();

        String content = Files.readString(configFile);
        assertTrue(content.contains("# The hostname to bind to"));
        assertTrue(content.contains("# The listening port"));
        assertTrue(content.contains("# Change this to 443 for production"));
    }
}
