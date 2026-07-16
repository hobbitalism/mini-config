package com.github.hobbitalism.miniconfig.bukkit.convert;

import com.github.hobbitalism.miniconfig.annotation.Config;
import com.github.hobbitalism.miniconfig.annotation.Path;
import com.github.hobbitalism.miniconfig.bukkit.BukkitConfig;
import com.github.hobbitalism.miniconfig.container.ConfigContainer;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OfflinePlayerConverterTest {

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

    @Config
    static class PlayerConfig {
        @Path("owner")
        UUID owner;
    }

    @Test
    void offlinePlayerConversion() throws IOException {
        UUID uuid = UUID.randomUUID();

        BukkitConfig config = new BukkitConfig(plugin, "players.yml");
        config.load();
        config.set("owner", uuid.toString());
        config.save();

        ConfigContainer container = new ConfigContainer(BukkitConverterRegistry.create());

        PlayerConfig pojo = new PlayerConfig();
        container.load(pojo, config);

        assertEquals(uuid, pojo.owner);
    }

    @Test
    void offlinePlayer_roundTrip() throws IOException {
        UUID original = UUID.randomUUID();

        BukkitConfig config = new BukkitConfig(plugin, "players.yml");
        config.load();
        config.set("owner", original.toString());
        config.save();

        ConfigContainer container = new ConfigContainer(BukkitConverterRegistry.create());

        PlayerConfig pojo = new PlayerConfig();
        container.load(pojo, config);
        assertEquals(original, pojo.owner);

        container.save(pojo, config);
        Object serialized = config.get("owner").orElse(null);
        assertEquals(original.toString(), serialized);
    }
}
