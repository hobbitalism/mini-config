package com.github.hobbitalism.miniconfig.bukkit.convert;

import com.github.hobbitalism.miniconfig.convert.ConversionException;
import com.github.hobbitalism.miniconfig.convert.TypeConverter;
import org.bukkit.Material;

/**
 * {@link TypeConverter} for {@link Material}.
 *
 * <p>Deserializes a config string to a {@link Material} via
 * {@link Material#matchMaterial(String)}, which is case-insensitive and
 * accepts both plain names ({@code "DIAMOND"}) and namespaced keys
 * ({@code "minecraft:diamond"}).
 *
 * <p>Serializes back to {@link Material#name()} (upper-case plain name),
 * which is the conventional format in Bukkit config files.
 *
 * <p>This converter is pre-registered in {@link BukkitConverterRegistry} for
 * the {@link Material} type, and is also the backing implementation of the
 * {@link com.github.hobbitalism.miniconfig.bukkit.annotation.Material @Material}
 * annotation.
 */
public class MaterialConverter implements TypeConverter<Material> {

    @Override
    public Material deserialize(Object raw) {
        String name = raw.toString().trim();
        Material material = Material.matchMaterial(name);
        if (material == null) {
            throw new ConversionException("Unknown material: '" + name + "'");
        }
        return material;
    }

    @Override
    public Object serialize(Material value) {
        return value.name();
    }
}
