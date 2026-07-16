package com.github.hobbitalism.miniconfig.bukkit.convert;

import com.github.hobbitalism.miniconfig.annotation.Config;
import com.github.hobbitalism.miniconfig.annotation.Converter;
import com.github.hobbitalism.miniconfig.annotation.Path;
import com.github.hobbitalism.miniconfig.bukkit.BukkitConfig;
import com.github.hobbitalism.miniconfig.container.ConfigContainer;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SoundConverterTest {

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
    static class SoundConfig {
        @Converter(SoundConverter.class)
        @Path("click-sound")
        Sound clickSound;
    }

    @Test
    void soundConversion() throws IOException {
        BukkitConfig config = new BukkitConfig(plugin, "sound.yml");
        config.load();
        config.set("click-sound", "minecraft:ui.button.click");
        config.save();

        ConfigContainer container = new ConfigContainer(BukkitConverterRegistry.create());

        SoundConfig pojo = new SoundConfig();
        container.load(pojo, config);

        assertNotNull(pojo.clickSound);
        assertEquals("minecraft:ui.button.click", pojo.clickSound.getKey().toString());
    }
}
