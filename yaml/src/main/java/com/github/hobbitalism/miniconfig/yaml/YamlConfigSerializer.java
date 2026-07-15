package com.github.hobbitalism.miniconfig.yaml;

import com.github.hobbitalism.miniconfig.ConfigSerializer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Writer;

/**
 * {@link ConfigSerializer} that writes a {@link YamlConfigSection} back to
 * YAML text using SnakeYAML.
 *
 * <p>Output uses block style with an indent of 2 spaces, which is the
 * conventional format for hand-edited config files.
 *
 * <pre>{@code
 * YamlConfigSerializer serializer = new YamlConfigSerializer();
 * serializer.save(section, new FileWriter("config.yml"));
 * }</pre>
 */
public class YamlConfigSerializer implements ConfigSerializer<YamlConfigSection> {

    private final DumperOptions options;

    /**
     * Constructs a serializer with default options (block style, indent 2).
     */
    public YamlConfigSerializer() {
        options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setIndent(2);
        options.setPrettyFlow(true);
    }

    /**
     * Constructs a serializer with custom {@link DumperOptions}.
     *
     * @param options the SnakeYAML dumper options to use
     */
    public YamlConfigSerializer(DumperOptions options) {
        this.options = options;
    }

    @Override
    public void save(YamlConfigSection section, Writer writer) throws IOException {
        Yaml yaml = new Yaml(options);
        yaml.dump(section.getRawData(), writer);
    }
}
