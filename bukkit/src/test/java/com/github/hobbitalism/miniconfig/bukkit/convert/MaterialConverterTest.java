package com.github.hobbitalism.miniconfig.bukkit.convert;

import com.github.hobbitalism.miniconfig.annotation.Config;
import com.github.hobbitalism.miniconfig.annotation.Path;
import com.github.hobbitalism.miniconfig.bukkit.BukkitConfig;
import com.github.hobbitalism.miniconfig.container.ConfigContainer;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class MaterialConverterTest {

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
    static class MaterialsConfig {
        @Path("main-material")
        Material mainMaterial;
    }

    @Test
    void materialConversion() throws IOException {
        BukkitConfig config = new BukkitConfig(plugin, "materials.yml");
        config.load();
        config.set("main-material", "DIAMOND_SWORD");
        config.save();

        ConfigContainer container = new ConfigContainer(BukkitConverterRegistry.create());

        MaterialsConfig pojo = new MaterialsConfig();
        container.load(pojo, config);

        assertEquals(Material.DIAMOND_SWORD, pojo.mainMaterial);
    }
}
