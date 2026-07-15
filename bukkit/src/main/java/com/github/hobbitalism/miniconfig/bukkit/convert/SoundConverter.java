package com.github.hobbitalism.miniconfig.bukkit.convert;

import com.github.hobbitalism.miniconfig.convert.ConversionException;
import com.github.hobbitalism.miniconfig.convert.TypeConverter;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

/**
 * {@link TypeConverter} for {@link Sound}.
 *
 * <p>In Bukkit 1.21+ {@link Sound} is a {@code Keyed} registry type rather than
 * a plain enum. This converter uses {@link Registry#SOUNDS} to look up sounds by
 * their {@link NamespacedKey}, so it is forward-compatible with future additions
 * without recompiling.
 *
 * <p>Accepted config formats:
 * <ul>
 *   <li>{@code "minecraft:entity.player.levelup"} — fully-qualified namespaced key</li>
 *   <li>{@code "entity.player.levelup"} — bare key, {@code minecraft:} namespace assumed</li>
 * </ul>
 *
 * <p>Serializes back to the full namespaced key string (e.g.
 * {@code "minecraft:entity.player.levelup"}).
 *
 * <p>This converter is pre-registered in {@link BukkitConverterRegistry} for the
 * {@link Sound} type, and is also the backing implementation of the
 * {@link com.github.hobbitalism.miniconfig.bukkit.annotation.Sound @Sound} annotation.
 */
public class SoundConverter implements TypeConverter<Sound> {

    @Override
    public Sound deserialize(Object raw) {
        String value = raw.toString().trim().toLowerCase();

        // Build a NamespacedKey — add the minecraft: namespace if absent
        NamespacedKey key;
        if (value.contains(":")) {
            key = NamespacedKey.fromString(value);
            if (key == null) {
                throw new ConversionException("Malformed namespaced key for sound: '" + raw + "'");
            }
        } else {
            key = NamespacedKey.minecraft(value);
        }

        Sound sound = Registry.SOUNDS.get(key);
        if (sound == null) {
            throw new ConversionException("Unknown sound: '" + raw + "'");
        }
        return sound;
    }

    @Override
    public Object serialize(Sound value) {
        return value.getKey().toString();
    }
}
