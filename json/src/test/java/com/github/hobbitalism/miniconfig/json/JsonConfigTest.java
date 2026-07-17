package com.github.hobbitalism.miniconfig.json;

import com.github.hobbitalism.miniconfig.annotation.Comment;
import com.github.hobbitalism.miniconfig.annotation.Config;
import com.github.hobbitalism.miniconfig.annotation.Default;
import com.github.hobbitalism.miniconfig.annotation.Path;
import com.github.hobbitalism.miniconfig.container.ConfigContainer;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JsonConfigTest {

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

    @Test
    void pojoRoundTrip() throws IOException {
        java.nio.file.Path tmpDir = java.nio.file.Path.of("build", "tmp");
        Files.createDirectories(tmpDir);

        UUID uuid = UUID.randomUUID();
        java.nio.file.Path configFile = tmpDir.resolve(uuid + ".json");
        System.out.println("Temp file: " + configFile.toAbsolutePath());

        JsonConfig config = new JsonConfig(configFile);
        config.load();

        ServerConfig pojo = new ServerConfig();
        pojo.host = "example.com";
        pojo.port = 8080;
        pojo.debug = true;
        pojo.worlds = List.of("nether", "end");

        ConfigContainer container = new ConfigContainer();
        container.save(pojo, config);
        config.save();

        JsonConfig reloaded = new JsonConfig(configFile);
        reloaded.load();

        ServerConfig result = new ServerConfig();
        container.load(result, reloaded);

        assertEquals("example.com", result.host);
        assertEquals(8080, result.port);
        assertTrue(result.debug);
        assertEquals(List.of("nether", "end"), result.worlds);
    }

    @Test
    void loadWithComments() throws IOException {
        String json = """
                {
                  // server settings
                  "server": {
                    "host": "example.com", /* inline comment */
                    "port": 8080
                  },
                  /* multi
                     line */
                  "worlds": ["nether", "end"]
                }
                """;

        ServerConfig result = new ServerConfig();
        ConfigContainer container = new ConfigContainer();
        container.load(result, new JsonConfigLoader().load(new StringReader(json)));

        assertEquals("example.com", result.host);
        assertEquals(8080, result.port);
        assertEquals(List.of("nether", "end"), result.worlds);
    }

    @Test
    void pojoWithComments_writesCommentLines() throws IOException {
        java.nio.file.Path tmpDir = java.nio.file.Path.of("build", "tmp");
        Files.createDirectories(tmpDir);

        java.nio.file.Path configFile = tmpDir.resolve(UUID.randomUUID() + ".json");
        System.out.println("Temp file: " + configFile.toAbsolutePath());

        JsonConfig config = new JsonConfig(configFile);
        config.load();

        CommentedConfig pojo = new CommentedConfig();
        pojo.host = "example.com";
        pojo.port = 8080;
        pojo.debug = true;

        ConfigContainer container = new ConfigContainer();
        container.save(pojo, config);
        config.save();

        String content = Files.readString(configFile);
        assertTrue(content.contains("// The hostname to bind to"));
        assertTrue(content.contains("// The listening port"));
        assertTrue(content.contains("// Change this to 443 for production"));
    }

    @Test
    void loadFromStream_loadsJsonData() throws IOException {
        String json = """
                {
                  "server": {
                    "host": "stream.example.com",
                    "port": 9090
                  },
                  "worlds": ["survival", "creative"]
                }
                """;
        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        java.nio.file.Path tmpDir = java.nio.file.Path.of("build", "tmp");
        Files.createDirectories(tmpDir);
        java.nio.file.Path configFile = tmpDir.resolve(UUID.randomUUID() + ".json");

        JsonConfig config = new JsonConfig(configFile);
        config.loadFromStream(stream);

        assertEquals("stream.example.com", config.getString("server.host", ""));
        assertEquals(9090, config.getInt("server.port", 0));
        assertEquals(List.of("survival", "creative"), config.getStringList("worlds"));
    }

    @Test
    void loadFromStream_pojoBinding() throws IOException {
        String json = """
                {
                  "server": {
                    "host": "pojo.example.com",
                    "port": 5555,
                    "debug": true
                  },
                  "worlds": ["nether", "end", "overworld"]
                }
                """;
        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        java.nio.file.Path tmpDir = java.nio.file.Path.of("build", "tmp");
        Files.createDirectories(tmpDir);
        java.nio.file.Path configFile = tmpDir.resolve(UUID.randomUUID() + ".json");

        JsonConfig config = new JsonConfig(configFile);
        config.loadFromStream(stream);

        ServerConfig pojo = new ServerConfig();
        ConfigContainer container = new ConfigContainer();
        container.load(pojo, config);

        assertEquals("pojo.example.com", pojo.host);
        assertEquals(5555, pojo.port);
        assertTrue(pojo.debug);
        assertEquals(List.of("nether", "end", "overworld"), pojo.worlds);
    }

    @Test
    void loadFromStream_withComments() throws IOException {
        String json = """
                {
                  // This is a comment
                  "server": {
                    "host": "commented.example.com",
                    "port": 7777 // inline comment
                  }
                }
                """;
        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        java.nio.file.Path tmpDir = java.nio.file.Path.of("build", "tmp");
        Files.createDirectories(tmpDir);
        java.nio.file.Path configFile = tmpDir.resolve(UUID.randomUUID() + ".json");

        JsonConfig config = new JsonConfig(configFile);
        config.loadFromStream(stream);

        assertEquals("commented.example.com", config.getString("server.host", ""));
        assertEquals(7777, config.getInt("server.port", 0));
    }

    @Test
    void loadFromStream_multipleLoads() throws IOException {
        String json1 = """
                {
                  "server": {
                    "host": "first.example.com",
                    "port": 1111
                  }
                }
                """;
        String json2 = """
                {
                  "server": {
                    "host": "second.example.com",
                    "port": 2222
                  }
                }
                """;

        java.nio.file.Path tmpDir = java.nio.file.Path.of("build", "tmp");
        Files.createDirectories(tmpDir);
        java.nio.file.Path configFile = tmpDir.resolve(UUID.randomUUID() + ".json");

        JsonConfig config = new JsonConfig(configFile);

        // Load first stream
        config.loadFromStream(new ByteArrayInputStream(json1.getBytes(StandardCharsets.UTF_8)));
        assertEquals("first.example.com", config.getString("server.host", ""));
        assertEquals(1111, config.getInt("server.port", 0));

        // Load second stream — should replace
        config.loadFromStream(new ByteArrayInputStream(json2.getBytes(StandardCharsets.UTF_8)));
        assertEquals("second.example.com", config.getString("server.host", ""));
        assertEquals(2222, config.getInt("server.port", 0));
    }

    @Test
    void loadFromStream_doesNotWriteToFile() throws IOException {
        String json = """
                {
                  "server": {
                    "host": "nofile.example.com",
                    "port": 3333
                  }
                }
                """;
        InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        java.nio.file.Path tmpDir = java.nio.file.Path.of("build", "tmp");
        Files.createDirectories(tmpDir);
        java.nio.file.Path configFile = tmpDir.resolve(UUID.randomUUID() + ".json");

        JsonConfig config = new JsonConfig(configFile);
        config.loadFromStream(stream);

        // Data is loaded in memory
        assertEquals("nofile.example.com", config.getString("server.host", ""));

        // But file should not exist yet
        assertFalse(Files.exists(configFile));

        // Only after save should it exist
        config.save();
        assertTrue(Files.exists(configFile));
    }
}
