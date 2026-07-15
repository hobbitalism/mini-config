package com.github.hobbitalism.miniconfig.yaml;

import com.github.hobbitalism.miniconfig.ConfigLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link ConfigLoader} that parses YAML documents into a {@link YamlConfigSection}
 * using SnakeYAML.
 *
 * <p>A new {@link Yaml} instance is created per load call to ensure thread safety
 * (SnakeYAML's {@code Yaml} is not thread-safe).
 *
 * <pre>{@code
 * YamlConfigLoader loader = new YamlConfigLoader();
 * YamlConfigSection section = loader.load(new FileReader("config.yml"));
 * }</pre>
 */
public class YamlConfigLoader implements ConfigLoader<YamlConfigSection> {

    @Override
    @SuppressWarnings("unchecked")
    public YamlConfigSection load(Reader reader) throws IOException {
        Yaml yaml = new Yaml();
        Object parsed = yaml.load(reader);
        if (parsed == null) {
            // Empty or blank document
            return new YamlConfigSection();
        }
        if (!(parsed instanceof Map)) {
            throw new IOException(
                    "YAML document root must be a mapping, got: " + parsed.getClass().getSimpleName());
        }
        return new YamlConfigSection(toStringKeyMap((Map<?, ?>) parsed));
    }

    /**
     * Recursively normalises a map so all keys are {@link String}.
     * SnakeYAML can produce non-string keys for complex YAML, but config
     * files are expected to have string keys only.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> toStringKeyMap(Map<?, ?> raw) {
        Map<String, Object> result = new LinkedHashMap<>(raw.size());
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object val = entry.getValue();
            if (val instanceof Map) {
                val = toStringKeyMap((Map<?, ?>) val);
            }
            result.put(key, val);
        }
        return result;
    }
}
