package com.github.hobbitalism.miniconfig.json;

import com.github.hobbitalism.miniconfig.ConfigSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JsonConfigSectionTest {

    private static final String JSON = """
            {
              "server": {
                "host": "localhost",
                "port": 25565,
                "debug": true
              },
              "messages": {
                "prefix": "&a[Server]",
                "motd": "Welcome!"
              },
              "worlds": ["world", "world_nether"]
            }
            """;

    private JsonConfigSection section;

    @BeforeEach
    void setUp() throws IOException {
        section = new JsonConfigLoader().load(new StringReader(JSON));
    }

    // ---- getString ----------------------------------------------------------

    @Test
    void getString_existingDotPath() {
        assertEquals("localhost", section.getString("server.host", ""));
    }

    @Test
    void getString_missingKey_returnsDefault() {
        assertEquals("default", section.getString("server.missing", "default"));
    }

    // ---- getInt -------------------------------------------------------------

    @Test
    void getInt_existingPath() {
        assertEquals(25565, section.getInt("server.port", 0));
    }

    // ---- getBoolean ---------------------------------------------------------

    @Test
    void getBoolean_existingPath() {
        assertTrue(section.getBoolean("server.debug", false));
    }

    // ---- getStringList ------------------------------------------------------

    @Test
    void getStringList_existingPath() {
        List<String> worlds = section.getStringList("worlds");
        assertEquals(List.of("world", "world_nether"), worlds);
    }

    @Test
    void getStringList_missingPath_returnsEmpty() {
        assertTrue(section.getStringList("nonexistent").isEmpty());
    }

    // ---- getSection ---------------------------------------------------------

    @Test
    void getSection_existingPath() {
        Optional<ConfigSection> server = section.getSection("server");
        assertTrue(server.isPresent());
        assertEquals("localhost", server.get().getString("host", ""));
    }

    @Test
    void getSection_missingPath_returnsEmpty() {
        assertTrue(section.getSection("nonexistent").isEmpty());
    }

    // ---- contains -----------------------------------------------------------

    @Test
    void contains_existingPath() {
        assertTrue(section.contains("server.host"));
    }

    @Test
    void contains_missingPath() {
        assertFalse(section.contains("server.missing"));
    }

    // ---- set ----------------------------------------------------------------

    @Test
    void set_newDotPath_createsIntermediateMaps() {
        section.set("database.host", "db.example.com");
        assertEquals("db.example.com", section.getString("database.host", ""));
    }

    @Test
    void set_null_removesKey() {
        section.set("server.debug", null);
        assertFalse(section.contains("server.debug"));
    }

    // ---- getKeys ------------------------------------------------------------

    @Test
    void getKeys_shallow_returnsTopLevelOnly() {
        var keys = section.getKeys(false);
        assertTrue(keys.contains("server"));
        assertTrue(keys.contains("messages"));
        assertTrue(keys.contains("worlds"));
        assertFalse(keys.contains("server.host"));
    }

    @Test
    void getKeys_deep_returnsNestedKeys() {
        var keys = section.getKeys(true);
        assertTrue(keys.contains("server.host"));
        assertTrue(keys.contains("server.port"));
    }

    // ---- round-trip ---------------------------------------------------------

    @Test
    void roundTrip_serializeThenReload_preservesValues() throws IOException {
        StringWriter writer = new StringWriter();
        new JsonConfigSerializer().save(section, writer);

        JsonConfigSection reloaded = new JsonConfigLoader().load(new StringReader(writer.toString()));
        assertEquals("localhost", reloaded.getString("server.host", ""));
        assertEquals(25565,        reloaded.getInt("server.port", 0));
    }

    // ---- empty document -----------------------------------------------------

    @Test
    void load_emptyObject_producesEmptySection() throws IOException {
        JsonConfigSection empty = new JsonConfigLoader().load(new StringReader("{}"));
        assertTrue(empty.getKeys(false).isEmpty());
    }
}
