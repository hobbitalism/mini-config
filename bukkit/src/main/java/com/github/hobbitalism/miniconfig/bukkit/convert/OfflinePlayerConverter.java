package com.github.hobbitalism.miniconfig.bukkit.convert;

import com.github.hobbitalism.miniconfig.convert.ConversionException;
import com.github.hobbitalism.miniconfig.convert.TypeConverter;

import java.util.UUID;

/**
 * {@link TypeConverter} for player identity stored as a {@link UUID}.
 *
 * <p>Config files store player identities as UUID strings rather than names,
 * because names can change while UUIDs are permanent. The actual
 * {@code OfflinePlayer} object is intentionally <em>not</em> the field type:
 * {@link org.bukkit.Bukkit#getOfflinePlayer(UUID)} performs blocking I/O and
 * should be called at runtime, not during config binding.
 *
 * <p>Accepted config format: the standard UUID string representation
 * ({@code "550e8400-e29b-41d4-a716-446655440000"}).
 *
 * <p>This converter is pre-registered in {@link BukkitConverterRegistry} and is
 * the backing implementation of the
 * {@link com.github.hobbitalism.miniconfig.bukkit.annotation.OfflinePlayer @OfflinePlayer}
 * annotation.
 */
public class OfflinePlayerConverter implements TypeConverter<UUID> {

    @Override
    public UUID deserialize(Object raw) {
        try {
            return UUID.fromString(raw.toString().trim());
        } catch (IllegalArgumentException e) {
            throw new ConversionException("Invalid UUID for offline player: '" + raw + "'", e);
        }
    }

    @Override
    public Object serialize(UUID value) {
        return value.toString();
    }
}
