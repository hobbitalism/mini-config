package com.github.hobbitalism.miniconfig.container;

import com.github.hobbitalism.miniconfig.annotation.Converter;
import com.github.hobbitalism.miniconfig.annotation.Default;
import com.github.hobbitalism.miniconfig.annotation.Ignore;
import com.github.hobbitalism.miniconfig.annotation.Path;
import com.github.hobbitalism.miniconfig.annotation.PostLoad;
import com.github.hobbitalism.miniconfig.annotation.PreSave;
import com.github.hobbitalism.miniconfig.convert.ConversionException;
import com.github.hobbitalism.miniconfig.convert.TypeConverter;
import com.github.hobbitalism.miniconfig.yaml.YamlConfigSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConfigContainerTest {

    private ConfigContainer container;

    @BeforeEach
    void setUp() {
        container = new ConfigContainer(ConverterRegistry.defaults());
    }

    // -------------------------------------------------------------------------
    // Fixture POJO
    // -------------------------------------------------------------------------

    static class ServerConfig {
        @Path("server.host")
        @Default("localhost")
        String host;

        @Path("server.port")
        @Default("25565")
        int port;

        @Path("server.debug")
        @Default("false")
        boolean debug;

        @Path("server.ratio")
        @Default("1.5")
        double ratio;

        @Ignore
        String ignored = "original";

        boolean postLoadCalled = false;
        boolean preSaveCalled  = false;

        @PostLoad
        void afterLoad() { postLoadCalled = true; }

        @PreSave
        void beforeSave() { preSaveCalled = true; }
    }

    // -------------------------------------------------------------------------
    // Load: values present in section
    // -------------------------------------------------------------------------

    @Test
    void load_valuesFromSection() throws IOException {
        YamlConfigSection section = yaml("""
                server:
                  host: myserver.com
                  port: 19132
                  debug: true
                  ratio: 2.5
                """);

        ServerConfig cfg = new ServerConfig();
        container.load(cfg, section);

        assertEquals("myserver.com", cfg.host);
        assertEquals(19132, cfg.port);
        assertTrue(cfg.debug);
        assertEquals(2.5, cfg.ratio);
    }

    // -------------------------------------------------------------------------
    // Load: @Default fallback
    // -------------------------------------------------------------------------

    @Test
    void load_defaultsUsedWhenPathAbsent() throws IOException {
        YamlConfigSection section = yaml("{}");

        ServerConfig cfg = new ServerConfig();
        container.load(cfg, section);

        assertEquals("localhost", cfg.host);
        assertEquals(25565, cfg.port);
        assertFalse(cfg.debug);
        assertEquals(1.5, cfg.ratio);
    }

    // -------------------------------------------------------------------------
    // Load: @Ignore respected
    // -------------------------------------------------------------------------

    @Test
    void load_ignoredFieldNotTouched() throws IOException {
        YamlConfigSection section = yaml("ignored: should-not-be-set");

        ServerConfig cfg = new ServerConfig();
        container.load(cfg, section);

        assertEquals("original", cfg.ignored);
    }

    // -------------------------------------------------------------------------
    // Lifecycle hooks
    // -------------------------------------------------------------------------

    @Test
    void load_postLoadCalled() throws IOException {
        ServerConfig cfg = new ServerConfig();
        container.load(cfg, yaml("{}"));
        assertTrue(cfg.postLoadCalled);
    }

    @Test
    void save_preSaveCalled() throws IOException {
        ServerConfig cfg = new ServerConfig();
        cfg.host = "x";
        cfg.port = 1;
        YamlConfigSection section = new YamlConfigSection();
        container.save(cfg, section);
        assertTrue(cfg.preSaveCalled);
    }

    // -------------------------------------------------------------------------
    // Save: round-trip
    // -------------------------------------------------------------------------

    @Test
    void save_thenLoad_roundTrip() throws IOException {
        ServerConfig original = new ServerConfig();
        original.host  = "roundtrip.host";
        original.port  = 9999;
        original.debug = true;
        original.ratio = 3.14;

        YamlConfigSection section = new YamlConfigSection();
        container.save(original, section);

        ServerConfig restored = new ServerConfig();
        container.load(restored, section);

        assertEquals("roundtrip.host", restored.host);
        assertEquals(9999,             restored.port);
        assertTrue(restored.debug);
        assertEquals(3.14,             restored.ratio, 1e-9);
    }

    // -------------------------------------------------------------------------
    // UUID via registry
    // -------------------------------------------------------------------------

    static class UuidConfig {
        @Path("owner")
        UUID owner;
    }

    @Test
    void load_uuidFromString() throws IOException {
        String id = "550e8400-e29b-41d4-a716-446655440000";
        YamlConfigSection section = yaml("owner: \"" + id + "\"");

        UuidConfig cfg = new UuidConfig();
        container.load(cfg, section);

        assertEquals(UUID.fromString(id), cfg.owner);
    }

    // -------------------------------------------------------------------------
    // @Converter on field
    // -------------------------------------------------------------------------

    public static class DurationConverter implements TypeConverter<Duration> {
        @Override public Duration deserialize(Object raw) { return Duration.parse(raw.toString()); }
        @Override public Object serialize(Duration value) { return value.toString(); }
    }

    static class TimerConfig {
        @Converter(DurationConverter.class)
        @Path("timeout")
        Duration timeout;
    }

    @Test
    void load_customConverterOnField() throws IOException {
        YamlConfigSection section = yaml("timeout: PT30S");

        TimerConfig cfg = new TimerConfig();
        container.load(cfg, section);

        assertEquals(Duration.ofSeconds(30), cfg.timeout);
    }

    @Test
    void save_customConverterOnField() {
        TimerConfig cfg = new TimerConfig();
        cfg.timeout = Duration.ofMinutes(5);

        YamlConfigSection section = new YamlConfigSection();
        container.save(cfg, section);

        assertEquals("PT5M", section.getString("timeout", ""));
    }

    // -------------------------------------------------------------------------
    // Enum fallback
    // -------------------------------------------------------------------------

    enum Difficulty { EASY, NORMAL, HARD }

    static class GameConfig {
        @Path("difficulty")
        Difficulty difficulty;
    }

    @Test
    void load_enumByName() throws IOException {
        YamlConfigSection section = yaml("difficulty: HARD");

        GameConfig cfg = new GameConfig();
        container.load(cfg, section);

        assertEquals(Difficulty.HARD, cfg.difficulty);
    }

    // -------------------------------------------------------------------------
    // Error: no converter
    // -------------------------------------------------------------------------

    static class BadConfig {
        @Path("thing")
        Object thing;  // Object has no registered converter
    }

    @Test
    void load_noConverter_throws() throws IOException {
        YamlConfigSection section = yaml("thing: value");
        BadConfig cfg = new BadConfig();
        assertThrows(ConfigBindingException.class, () -> container.load(cfg, section));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static YamlConfigSection yaml(String content) throws IOException {
        return new com.github.hobbitalism.miniconfig.yaml.YamlConfigLoader()
                .load(new StringReader(content));
    }
}
